<?php
// backend/api/auth/send_otp.php

require_once '../../config/db.php';
require_once '../../helpers/response.php';
require_once '../../helpers/sms.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);

// Extract parameters
$phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : '';

if (empty($phoneNumber)) {
    Response::send(false, "Phone number is required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Check if phone number is already registered in the users table
    $userQuery = "SELECT user_id FROM users WHERE phone_number = ? LIMIT 1";
    $userStmt = $db->prepare($userQuery);
    $userStmt->execute([$phoneNumber]);
    $isRegistered = ($userStmt->rowCount() > 0);

    // 2. Generate a random 6-digit OTP
    $otp = str_pad(rand(100000, 999999), 6, '0', STR_PAD_LEFT);

    if ($isRegistered) {
        // Already registered -> OTP for passwordless login
        $expiresAt = date('Y-m-d H:i:s', time() + 300); // 5 minutes expiration
        $otpQuery = "INSERT INTO otp_verifications (phone_number, otp_code, resend_count, expires_at, is_used) 
                     VALUES (?, ?, 1, ?, 0) 
                     ON DUPLICATE KEY UPDATE otp_code = VALUES(otp_code), expires_at = VALUES(expires_at), is_used = 0";
        $otpStmt = $db->prepare($otpQuery);
        $otpStmt->execute([$phoneNumber, $otp, $expiresAt]);
    } else {
        // Not registered -> OTP for signup/registration flow
        $otpQuery = "INSERT INTO phone_otps (phone_number, otp_code) 
                     VALUES (?, ?) 
                     ON DUPLICATE KEY UPDATE otp_code = VALUES(otp_code)";
        $otpStmt = $db->prepare($otpQuery);
        $otpStmt->execute([$phoneNumber, $otp]);
    }

    // 4. Send the OTP message via SMS Gateway
    $message = "Your SafeReach signup verification code is: $otp. It is valid for 5 minutes.";
    $smsResult = SMSGateway::send($phoneNumber, $message);

    // 5. Return success response
    $data = [
        "phone_number" => $phoneNumber,
        "mock_otp" => (!$smsResult['success']) ? $otp : null
    ];

    Response::send(true, "Verification OTP code sent successfully.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to send verification code: " . $e->getMessage(), null, 500);
}
?>
