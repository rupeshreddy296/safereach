<?php
// backend/api/auth/send_otp.php

require_once '../../config/db.php';
require_once '../../helpers/response.php';
require_once '../../helpers/sms.php';
require_once '../../helpers/mailer.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);

// Extract parameters
$phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : '';
$email = isset($input['email']) ? trim($input['email']) : '';

if (empty($phoneNumber) && empty($email)) {
    Response::send(false, "Phone number or email is required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Check if user is already registered in the users table
    $userQuery = "SELECT user_id, email, full_name, phone_number FROM users WHERE (phone_number != '' AND phone_number = ?) OR (email != '' AND email = ?) LIMIT 1";
    $userStmt = $db->prepare($userQuery);
    $userStmt->execute([$phoneNumber, $email]);
    $userRow = $userStmt->fetch(PDO::FETCH_ASSOC);
    $isRegistered = !empty($userRow);

    // If email wasn't provided but phone was, retrieve registered email
    if (empty($email) && $isRegistered && !empty($userRow['email'])) {
        $email = $userRow['email'];
    }

    // 2. Generate a random 6-digit OTP
    $otp = str_pad(rand(100000, 999999), 6, '0', STR_PAD_LEFT);
    $expiresAt = date('Y-m-d H:i:s', time() + 600); // 10 minutes expiration

    // 3. Store OTP
    if (!empty($phoneNumber)) {
        if ($isRegistered) {
            $otpQuery = "INSERT INTO otp_verifications (phone_number, otp_code, resend_count, expires_at, is_used) 
                         VALUES (?, ?, 1, ?, 0) 
                         ON DUPLICATE KEY UPDATE otp_code = VALUES(otp_code), expires_at = VALUES(expires_at), is_used = 0";
            $otpStmt = $db->prepare($otpQuery);
            $otpStmt->execute([$phoneNumber, $otp, $expiresAt]);
        } else {
            $otpQuery = "INSERT INTO phone_otps (phone_number, otp_code) 
                         VALUES (?, ?) 
                         ON DUPLICATE KEY UPDATE otp_code = VALUES(otp_code)";
            $otpStmt = $db->prepare($otpQuery);
            $otpStmt->execute([$phoneNumber, $otp]);
        }
    }

    // 4. Send the OTP message via SMS Gateway (if phone number available)
    $smsResult = ['success' => false];
    if (!empty($phoneNumber)) {
        $message = "Your SafeReach verification code is: $otp. It is valid for 10 minutes.";
        $smsResult = SMSGateway::send($phoneNumber, $message);
    }

    // 5. Send OTP via SMTP Email (if email available)
    $emailResult = ['success' => false];
    if (!empty($email)) {
        $recipientName = !empty($userRow['full_name']) ? $userRow['full_name'] : 'User';
        $emailResult = Mailer::sendOTP($email, $otp, "Signup & Login", $recipientName);
    }

    // 6. Return response
    $delivered = $smsResult['success'] || $emailResult['success'];
    $data = [
        "phone_number" => $phoneNumber,
        "email"        => $email,
        "sms_sent"     => $smsResult['success'],
        "email_sent"   => $emailResult['success'],
        "expires_at"   => $expiresAt,
        "mock_otp"     => (!$delivered) ? $otp : null
    ];

    $msg = $delivered ? "Verification OTP code sent successfully." : "Verification OTP generated.";
    Response::send(true, $msg, $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to send verification code: " . $e->getMessage(), null, 500);
}
?>
