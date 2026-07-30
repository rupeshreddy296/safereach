<?php
// backend/api/auth/login_verify.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);

// Extract parameters
$phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : '';
$otp = isset($input['otp']) ? trim($input['otp']) : '';

if (empty($phoneNumber) || empty($otp)) {
    Response::send(false, "Phone number and OTP code are required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Verify OTP
    $otpQuery = "SELECT id, otp_code, expires_at, is_used FROM otp_verifications 
                 WHERE phone_number = ? AND is_used = 0 ORDER BY id DESC LIMIT 1";
    $otpStmt = $db->prepare($otpQuery);
    $otpStmt->execute([$phoneNumber]);
    $otpRecord = $otpStmt->fetch();

    if (!$otpRecord) {
        Response::send(false, "No active OTP verification request found for this phone number.", null, 400);
    }

    // Check expiration
    if (strtotime($otpRecord['expires_at']) < time()) {
        Response::send(false, "OTP has expired. Please request a new verification code.", null, 400);
    }

    // Check match
    if ($otpRecord['otp_code'] !== $otp) {
        Response::send(false, "Invalid OTP code. Please check and try again.", null, 400);
    }

    // Start database transaction
    $db->beginTransaction();

    // 2. Mark OTP as used
    $updateOtpQuery = "UPDATE otp_verifications SET is_used = 1 WHERE id = ?";
    $updateOtpStmt = $db->prepare($updateOtpQuery);
    $updateOtpStmt->execute([$otpRecord['id']]);

    // 3. Fetch User Profile
    $userQuery = "SELECT user_id, full_name, email, role, hospital_id FROM users WHERE phone_number = ? LIMIT 1";
    $userStmt = $db->prepare($userQuery);
    $userStmt->execute([$phoneNumber]);
    $user = $userStmt->fetch();

    if (!$user) {
        $db->rollBack();
        Response::send(false, "User profile not found.", null, 404);
    }

    // 4. Generate Bearer JWT token
    $payload = [
        "user_id" => (int)$user['user_id'],
        "email" => $user['email'],
        "role" => $user['role'],
        "hospital_id" => $user['hospital_id'] !== null ? (int)$user['hospital_id'] : null
    ];
    $token = JWT::encode($payload);

    // 5. Store session in login_sessions table
    $sessionExpiry = date('Y-m-d H:i:s', time() + (86400 * 30)); // 30 days session
    $sessionQuery = "INSERT INTO login_sessions (user_id, token, expires_at) VALUES (?, ?, ?)";
    $sessionStmt = $db->prepare($sessionQuery);
    $sessionStmt->execute([$user['user_id'], $token, $sessionExpiry]);

    // 6. Fetch Ambulance Details (If Driver)
    $vehicle = null;
    if ($user['role'] === 'driver') {
        $ambulanceQuery = "SELECT ambulance_id, vehicle_number, ambulance_type, status FROM ambulances WHERE driver_id = ? LIMIT 1";
        $ambulanceStmt = $db->prepare($ambulanceQuery);
        $ambulanceStmt->execute([$user['user_id']]);
        $ambulance = $ambulanceStmt->fetch();
        
        if ($ambulance) {
            $vehicle = [
                "ambulance_id" => (int)$ambulance['ambulance_id'],
                "vehicle_number" => $ambulance['vehicle_number'],
                "ambulance_type" => $ambulance['ambulance_type'],
                "status" => $ambulance['status']
            ];
        }
    }

    // 7. Fetch Hospital Details (If linked)
    $hospital = null;
    if ($user['hospital_id'] !== null) {
        $hospQuery = "SELECT hospital_id, name, address FROM hospitals WHERE hospital_id = ? LIMIT 1";
        $hospStmt = $db->prepare($hospQuery);
        $hospStmt->execute([$user['hospital_id']]);
        $hospRow = $hospStmt->fetch();
        if ($hospRow) {
            $hospital = [
                "hospital_id" => (int)$hospRow['hospital_id'],
                "name" => $hospRow['name'],
                "address" => $hospRow['address']
            ];
        }
    }

    $db->commit();

    // Prepare response data
    $data = [
        "token" => $token,
        "user_id" => (int)$user['user_id'],
        "full_name" => $user['full_name'],
        "email" => $user['email'],
        "role" => $user['role'],
        "vehicle" => $vehicle,
        "hospital" => $hospital
    ];

    Response::send(true, "Login verification successful.", $data, 200);

} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    Response::send(false, "Login verification failed: " . $e->getMessage(), null, 500);
}
?>
