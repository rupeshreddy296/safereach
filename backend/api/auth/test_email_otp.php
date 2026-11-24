<?php
// backend/api/auth/test_email_otp.php
// Quick endpoint to test SMTP email OTP dispatch

header("Content-Type: application/json; charset=UTF-8");
require_once '../../config/smtp.php';
require_once '../../helpers/mailer.php';
require_once '../../helpers/response.php';

$input = json_decode(file_get_contents("php://input"), true);
$targetEmail = isset($input['email']) ? trim($input['email']) : (isset($_GET['email']) ? trim($_GET['email']) : 'rupesh112599@gmail.com');

$testOtp = str_pad(rand(100000, 999999), 6, '0', STR_PAD_LEFT);

$result = Mailer::sendOTP($targetEmail, $testOtp, "SafeReach Test", "Rupesh");

Response::send($result['success'], $result['message'], [
    "target_email" => $targetEmail,
    "otp_code"     => $testOtp,
    "smtp_result"  => $result
], $result['success'] ? 200 : 500);
?>
