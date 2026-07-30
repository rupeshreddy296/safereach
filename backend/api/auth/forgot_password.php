<?php
// backend/api/auth/forgot_password.php

require_once '../../config/db.php';
require_once '../../helpers/response.php';
require_once '../../helpers/sms.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

$input = json_decode(file_get_contents("php://input"), true);
$phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : '';

if (empty($phoneNumber)) {
    Response::send(false, "Phone number is required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Check if user exists
    $userQuery = "SELECT user_id FROM users WHERE phone_number = ? LIMIT 1";
    $userStmt = $db->prepare($userQuery);
    $userStmt->execute([$phoneNumber]);
    if (!$userStmt->fetch()) {
        Response::send(false, "No account found with this phone number.", null, 404);
    }

    // 2. Generate 6-digit OTP
    $otpCode = sprintf("%06d", mt_rand(1, 999999));
    $expiresAt = date('Y-m-d H:i:s', strtotime('+10 minutes'));

    // 3. Store OTP in database (Upsert)
    $otpQuery = "INSERT INTO otp_verifications (phone_number, otp_code, expires_at, is_used)
                 VALUES (?, ?, ?, 0)
                 ON DUPLICATE KEY UPDATE
                 otp_code = VALUES(otp_code),
                 expires_at = VALUES(expires_at),
                 is_used = 0,
                 resend_count = resend_count + 1";
    $otpStmt = $db->prepare($otpQuery);
    $otpStmt->execute([$phoneNumber, $otpCode, $expiresAt]);

    // 4. Send SMS
    $message = "Your SAFEREACH verification code is: $otpCode. Valid for 10 minutes.";
    $smsResult = SMSGateway::send($phoneNumber, $message);

    if ($smsResult['success']) {
        Response::send(true, "Verification code sent to $phoneNumber.", ["expires_at" => $expiresAt], 200);
    } else {
        // For development/XAMPP, we might want to return the OTP in response if SMS fails
        Response::send(true, "Verification code generated (SMS gateway offline).", ["otp_debug" => $otpCode, "expires_at" => $expiresAt], 200);
    }

} catch (Exception $e) {
    Response::send(false, "Error processing forgot password request: " . $e->getMessage(), null, 500);
}
?>
