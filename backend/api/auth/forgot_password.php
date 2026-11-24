<?php
// backend/api/auth/forgot_password.php

require_once '../../config/db.php';
require_once '../../helpers/response.php';
require_once '../../helpers/sms.php';
require_once '../../helpers/mailer.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

$input = json_decode(file_get_contents("php://input"), true);
$phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : '';
$email = isset($input['email']) ? trim($input['email']) : '';

if (empty($phoneNumber) && empty($email)) {
    Response::send(false, "Phone number or email is required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Check if user exists by phone or email
    $userQuery = "SELECT user_id, full_name, email, phone_number FROM users WHERE (phone_number != '' AND phone_number = ?) OR (email != '' AND email = ?) LIMIT 1";
    $userStmt = $db->prepare($userQuery);
    $userStmt->execute([$phoneNumber, $email]);
    $user = $userStmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        Response::send(false, "No account found with provided credentials.", null, 404);
    }

    $targetPhone = !empty($user['phone_number']) ? $user['phone_number'] : $phoneNumber;
    $targetEmail = !empty($user['email']) ? $user['email'] : $email;
    $recipientName = !empty($user['full_name']) ? $user['full_name'] : 'SafeReach User';

    // 2. Generate 6-digit OTP
    $otpCode = sprintf("%06d", mt_rand(100000, 999999));
    $expiresAt = date('Y-m-d H:i:s', strtotime('+10 minutes'));

    // 3. Store OTP in database (Upsert)
    if (!empty($targetPhone)) {
        $otpQuery = "INSERT INTO otp_verifications (phone_number, otp_code, expires_at, is_used)
                     VALUES (?, ?, ?, 0)
                     ON DUPLICATE KEY UPDATE
                     otp_code = VALUES(otp_code),
                     expires_at = VALUES(expires_at),
                     is_used = 0,
                     resend_count = resend_count + 1";
        $otpStmt = $db->prepare($otpQuery);
        $otpStmt->execute([$targetPhone, $otpCode, $expiresAt]);
    }

    // 4. Send SMS (if phone available)
    $smsResult = ['success' => false];
    if (!empty($targetPhone)) {
        $message = "Your SAFEREACH verification code is: $otpCode. Valid for 10 minutes.";
        $smsResult = SMSGateway::send($targetPhone, $message);
    }

    // 5. Send Email via SMTP (if email available)
    $emailResult = ['success' => false];
    if (!empty($targetEmail)) {
        $emailResult = Mailer::sendOTP($targetEmail, $otpCode, "Password Reset", $recipientName);
    }

    $delivered = $smsResult['success'] || $emailResult['success'];

    $data = [
        "phone_number" => $targetPhone,
        "email"        => $targetEmail,
        "sms_sent"     => $smsResult['success'],
        "email_sent"   => $emailResult['success'],
        "expires_at"   => $expiresAt
    ];

    if (!$delivered) {
        $data["otp_debug"] = $otpCode;
    }

    $msg = $delivered 
        ? "Verification code sent successfully to your registered credentials." 
        : "Verification code generated.";

    Response::send(true, $msg, $data, 200);

} catch (Exception $e) {
    Response::send(false, "Error processing forgot password request: " . $e->getMessage(), null, 500);
}
?>
