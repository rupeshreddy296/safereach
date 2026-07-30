<?php
// backend/api/user/profile.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$userId = $userPayload['user_id'];

$database = new Database();
$db = $database->getConnection();

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    try {
        $query = "SELECT user_id, full_name, phone_number, email, role, 
                         emergency_contact_name, emergency_contact_phone, 
                         blood_group, allergies, chronic_conditions, created_at 
                  FROM users WHERE user_id = ? LIMIT 1";
        $stmt = $db->prepare($query);
        $stmt->execute([$userId]);
        $profile = $stmt->fetch();
        
        if (!$profile) {
            Response::send(false, "User profile not found.", null, 404);
        }

        // Map database fields to UserProfile model format
        $data = [
            "user_id" => (int)$profile['user_id'],
            "full_name" => $profile['full_name'],
            "phone_number" => $profile['phone_number'],
            "email" => $profile['email'],
            "role" => $profile['role'],
            "emergency_contact_name" => $profile['emergency_contact_name'],
            "emergency_contact_phone" => $profile['emergency_contact_phone'],
            "blood_group" => $profile['blood_group'],
            "allergies" => $profile['allergies'],
            "chronic_conditions" => $profile['chronic_conditions'],
            "created_at" => $profile['created_at']
        ];

        Response::send(true, "Profile retrieved successfully.", $data, 200);

    } catch (Exception $e) {
        Response::send(false, "Failed to retrieve profile: " . $e->getMessage(), null, 500);
    }
} elseif ($method === 'PUT') {
    // Get raw JSON input
    $input = json_decode(file_get_contents("php://input"), true);

    try {
        // Fetch current profile to ensure it exists
        $query = "SELECT user_id FROM users WHERE user_id = ? LIMIT 1";
        $stmt = $db->prepare($query);
        $stmt->execute([$userId]);
        if ($stmt->rowCount() === 0) {
            Response::send(false, "User profile not found.", null, 404);
        }

        // Construct update query dynamically based on provided fields
        $allowedFields = [
            'full_name', 'email', 'emergency_contact_name', 
            'emergency_contact_phone', 'blood_group', 'allergies', 'chronic_conditions'
        ];
        
        $updates = [];
        $params = [];
        
        foreach ($allowedFields as $field) {
            if (isset($input[$field])) {
                $updates[] = "$field = ?";
                $params[] = trim($input[$field]);
            }
        }

        if (empty($updates)) {
            Response::send(false, "No fields specified for update.", null, 400);
        }

        // Append userId to params
        $params[] = $userId;
        
        $updateQuery = "UPDATE users SET " . implode(', ', $updates) . " WHERE user_id = ?";
        $updateStmt = $db->prepare($updateQuery);
        $updateStmt->execute($params);

        // Fetch updated profile
        $selectQuery = "SELECT user_id, full_name, phone_number, email, role, 
                               emergency_contact_name, emergency_contact_phone, 
                               blood_group, allergies, chronic_conditions, created_at 
                        FROM users WHERE user_id = ? LIMIT 1";
        $selectStmt = $db->prepare($selectQuery);
        $selectStmt->execute([$userId]);
        $profile = $selectStmt->fetch();

        $data = [
            "user_id" => (int)$profile['user_id'],
            "full_name" => $profile['full_name'],
            "phone_number" => $profile['phone_number'],
            "email" => $profile['email'],
            "role" => $profile['role'],
            "emergency_contact_name" => $profile['emergency_contact_name'],
            "emergency_contact_phone" => $profile['emergency_contact_phone'],
            "blood_group" => $profile['blood_group'],
            "allergies" => $profile['allergies'],
            "chronic_conditions" => $profile['chronic_conditions'],
            "created_at" => $profile['created_at']
        ];

        Response::send(true, "Profile updated successfully.", $data, 200);

    } catch (Exception $e) {
        Response::send(false, "Failed to update profile: " . $e->getMessage(), null, 500);
    }
} else {
    Response::send(false, "Method not allowed. Use GET or PUT.", null, 405);
}
?>
