<?php
// backend/api/auth/change_password.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

$userPayload = Auth::getAuthenticatedUser();
if (!$userPayload) {
    Response::send(false, "Unauthorized.", null, 401);
}
$userId = $userPayload['user_id'];

$input = json_decode(file_get_contents("php://input"), true);
$oldPassword = isset($input['old_password']) ? trim($input['old_password']) : '';
$newPassword = isset($input['new_password']) ? trim($input['new_password']) : '';

if (empty($oldPassword) || empty($newPassword)) {
    Response::send(false, "Missing current password or new password.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    $query = "SELECT password_hash FROM users WHERE user_id = ? LIMIT 1";
    $stmt = $db->prepare($query);
    $stmt->execute([$userId]);
    $user = $stmt->fetch();
    
    if (!$user) {
        Response::send(false, "User not found.", null, 404);
    }
    
    if (!password_verify($oldPassword, $user['password_hash'])) {
        Response::send(false, "Current password incorrect.", null, 400);
    }
    
    $newHash = password_hash($newPassword, PASSWORD_BCRYPT);
    $updateQuery = "UPDATE users SET password_hash = ? WHERE user_id = ?";
    $updateStmt = $db->prepare($updateQuery);
    $updateStmt->execute([$newHash, $userId]);
    
    Response::send(true, "Password changed successfully.", null, 200);
} catch (Exception $e) {
    Response::send(false, "Database error: " . $e->getMessage(), null, 500);
}
?>
