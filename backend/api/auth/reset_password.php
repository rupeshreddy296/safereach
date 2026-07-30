<?php
// backend/api/auth/reset_password.php

require_once '../../config/db.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

$input = json_decode(file_get_contents("php://input"), true);
$phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : '';
$otpCode = isset($input['otp_code']) ? trim($input['otp_code']) : '';
$newPassword = isset($input['new_password']) ? trim($input['new_password']) : '';

if (empty($phoneNumber) || empty($otpCode) || empty($newPassword)) {
    Response::send(false, "Phone number, OTP code, and new password are required.", null, 400);
}

if (strlen($newPassword) < 6) {
    Response::send(false, "Password must be at least 6 characters.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    $db->beginTransaction();

    // 1. Verify OTP
    $otpQuery = "SELECT id FROM otp_verifications
                 WHERE phone_number = ? AND otp_code = ? AND expires_at > NOW() AND is_used = 0
                 LIMIT 1";
    $otpStmt = $db->prepare($otpQuery);
    $otpStmt->execute([$phoneNumber, $otpCode]);
    $otp = $otpStmt->fetch();

    if (!$otp) {
        Response::send(false, "Invalid or expired verification code.", null, 400);
    }

    // 2. Hash new password
    $passwordHash = password_hash($newPassword, PASSWORD_BCRYPT);

    // 3. Update user password
    $updateQuery = "UPDATE users SET password_hash = ? WHERE phone_number = ?";
    $updateStmt = $db->prepare($updateQuery);
    $updateStmt->execute([$passwordHash, $phoneNumber]);

    // 4. Mark OTP as used
    $markUsed = "UPDATE otp_verifications SET is_used = 1 WHERE id = ?";
    $markUsedStmt = $db->prepare($markUsed);
    $markUsedStmt->execute([$otp['id']]);

    // 5. Revoke all existing sessions for security
    $revokeSessions = "DELETE FROM login_sessions WHERE user_id = (SELECT user_id FROM users WHERE phone_number = ?)";
    $revokeStmt = $db->prepare($revokeSessions);
    $revokeStmt->execute([$phoneNumber]);

    $db->commit();
    Response::send(true, "Password has been reset successfully. Please login with your new password.", null, 200);

} catch (Exception $e) {
    if ($db->inTransaction()) $db->rollBack();
    Response::send(false, "Failed to reset password: " . $e->getMessage(), null, 500);
}
?>
