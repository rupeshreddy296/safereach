<?php
// backend/api/admin/users.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$currentUser = Auth::getAuthenticatedUser();
if (!in_array($currentUser['role'], ['super_admin', 'admin'])) {
    Response::send(false, "Forbidden. Administrative access required.", null, 403);
}

$method = $_SERVER['REQUEST_METHOD'];
$database = new Database();
$db = $database->getConnection();

try {
    switch ($method) {
        case 'GET':
            // Read Users List
            $query = "SELECT u.user_id, u.full_name, u.phone_number, u.email, u.role, u.hospital_id, u.created_at,
                             h.name AS hospital_name
                      FROM users u
                      LEFT JOIN hospitals h ON u.hospital_id = h.hospital_id
                      ORDER BY u.created_at DESC";
            
            $stmt = $db->prepare($query);
            $stmt->execute();
            $users = $stmt->fetchAll(PDO::FETCH_ASSOC);
            
            Response::send(true, "Users list retrieved successfully.", $users, 200);
            break;

        case 'POST':
            // Create User
            $input = json_decode(file_get_contents("php://input"), true);
            $fullName = isset($input['full_name']) ? trim($input['full_name']) : '';
            $phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : '';
            $email = isset($input['email']) ? trim($input['email']) : '';
            $password = isset($input['password']) ? trim($input['password']) : '';
            $role = isset($input['role']) ? trim($input['role']) : 'user';
            $hospitalId = isset($input['hospital_id']) && (int)$input['hospital_id'] > 0 ? (int)$input['hospital_id'] : null;

            if (empty($fullName) || empty($phoneNumber) || empty($email) || empty($password)) {
                Response::send(false, "Required user fields are missing.", null, 400);
            }

            // Verify unique constraints
            $dupQuery = "SELECT user_id FROM users WHERE phone_number = ? OR email = ? LIMIT 1";
            $dupStmt = $db->prepare($dupQuery);
            $dupStmt->execute([$phoneNumber, $email]);
            if ($dupStmt->fetch()) {
                Response::send(false, "Phone number or email is already registered.", null, 409);
            }

            $passwordHash = password_hash($password, PASSWORD_BCRYPT);

            $insertQuery = "INSERT INTO users (full_name, phone_number, email, password_hash, role, hospital_id)
                            VALUES (?, ?, ?, ?, ?, ?)";
            $insertStmt = $db->prepare($insertQuery);
            $insertStmt->execute([$fullName, $phoneNumber, $email, $passwordHash, $role, $hospitalId]);
            $newUserId = $db->lastInsertId();

            // Log action in audit logs
            $logQuery = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, 'create_user', ?)";
            $logStmt = $db->prepare($logQuery);
            $logStmt->execute([$currentUser['user_id'], "Created user ID $newUserId with role $role"]);

            Response::send(true, "User created successfully.", ["user_id" => (int)$newUserId], 201);
            break;

        case 'PUT':
            // Update User
            $input = json_decode(file_get_contents("php://input"), true);
            $userId = isset($input['user_id']) ? (int)$input['user_id'] : 0;
            $fullName = isset($input['full_name']) ? trim($input['full_name']) : '';
            $phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : '';
            $email = isset($input['email']) ? trim($input['email']) : '';
            $role = isset($input['role']) ? trim($input['role']) : 'user';
            $hospitalId = isset($input['hospital_id']) && (int)$input['hospital_id'] > 0 ? (int)$input['hospital_id'] : null;

            if ($userId <= 0 || empty($fullName) || empty($phoneNumber) || empty($email)) {
                Response::send(false, "Required validation fields are missing.", null, 400);
            }

            $updateQuery = "UPDATE users 
                            SET full_name = ?, phone_number = ?, email = ?, role = ?, hospital_id = ?
                            WHERE user_id = ?";
            $updateStmt = $db->prepare($updateQuery);
            $updateStmt->execute([$fullName, $phoneNumber, $email, $role, $hospitalId, $userId]);

            // Optional password update
            if (!empty($input['password'])) {
                $passwordHash = password_hash($input['password'], PASSWORD_BCRYPT);
                $pwQuery = "UPDATE users SET password_hash = ? WHERE user_id = ?";
                $pwStmt = $db->prepare($pwQuery);
                $pwStmt->execute([$passwordHash, $userId]);
            }

            // Log action in audit logs
            $logQuery = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, 'update_user', ?)";
            $logStmt = $db->prepare($logQuery);
            $logStmt->execute([$currentUser['user_id'], "Updated user ID $userId details"]);

            Response::send(true, "User updated successfully.", null, 200);
            break;

        case 'DELETE':
            // Delete User
            $userId = isset($_GET['id']) ? (int)$_GET['id'] : 0;
            if ($userId <= 0) {
                Response::send(false, "Valid User ID query parameter is required.", null, 400);
            }

            if ($userId === (int)$currentUser['user_id']) {
                Response::send(false, "You cannot delete your own administrative session.", null, 400);
            }

            $deleteQuery = "DELETE FROM users WHERE user_id = ?";
            $deleteStmt = $db->prepare($deleteQuery);
            $deleteStmt->execute([$userId]);

            // Log action in audit logs
            $logQuery = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, 'delete_user', ?)";
            $logStmt = $db->prepare($logQuery);
            $logStmt->execute([$currentUser['user_id'], "Deleted user ID $userId"]);

            Response::send(true, "User deleted successfully.", null, 200);
            break;

        default:
            Response::send(false, "Method not allowed. Use GET, POST, PUT, or DELETE.", null, 405);
    }
} catch (Exception $e) {
    Response::send(false, "Database operation failed: " . $e->getMessage(), null, 500);
}
?>
