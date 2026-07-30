<?php
// backend/api/user/notifications.php

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
        $query = "SELECT notification_id, title, description, type, is_read, created_at 
                  FROM notifications 
                  WHERE user_id = ? 
                  ORDER BY created_at DESC";
        $stmt = $db->prepare($query);
        $stmt->execute([$userId]);
        $rows = $stmt->fetchAll();

        $data = [];
        foreach ($rows as $row) {
            $data[] = [
                "notification_id" => (int)$row['notification_id'],
                "title" => $row['title'],
                "description" => $row['description'],
                "type" => $row['type'],
                "is_read" => (bool)$row['is_read'],
                "created_at" => $row['created_at']
            ];
        }

        Response::send(true, "Notifications retrieved successfully.", $data, 200);

    } catch (Exception $e) {
        Response::send(false, "Failed to retrieve notifications: " . $e->getMessage(), null, 500);
    }
} elseif ($method === 'PUT') {
    // Get raw JSON input
    $input = json_decode(file_get_contents("php://input"), true);
    
    $notificationId = isset($input['notification_id']) ? (int)$input['notification_id'] : null;

    try {
        if ($notificationId !== null && $notificationId > 0) {
            // Verify ownership and update single notification
            $checkQuery = "SELECT notification_id FROM notifications WHERE notification_id = ? AND user_id = ? LIMIT 1";
            $checkStmt = $db->prepare($checkQuery);
            $checkStmt->execute([$notificationId, $userId]);
            
            if ($checkStmt->rowCount() === 0) {
                Response::send(false, "Notification not found or unauthorized.", null, 404);
            }

            $updateQuery = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
            $updateStmt = $db->prepare($updateQuery);
            $updateStmt->execute([$notificationId]);
        } else {
            // Update all notifications as read for this user
            $updateQuery = "UPDATE notifications SET is_read = 1 WHERE user_id = ?";
            $updateStmt = $db->prepare($updateQuery);
            $updateStmt->execute([$userId]);
        }

        Response::send(true, "Notifications marked as read successfully.", null, 200);

    } catch (Exception $e) {
        Response::send(false, "Failed to update notification read status: " . $e->getMessage(), null, 500);
    }
} else {
    Response::send(false, "Method not allowed. Use GET or PUT.", null, 405);
}
?>
