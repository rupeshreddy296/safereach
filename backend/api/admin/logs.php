<?php
// backend/api/admin/logs.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$currentUser = Auth::getAuthenticatedUser();
if (!in_array($currentUser['role'], ['super_admin', 'admin'])) {
    Response::send(false, "Forbidden. Administrative access required.", null, 403);
}

$type = isset($_GET['type']) ? trim($_GET['type']) : 'audit';
$database = new Database();
$db = $database->getConnection();

try {
    if ($type === 'sms') {
        // Fetch SMS Gateway logs
        $query = "SELECT log_id, phone_number, message, gateway, status, response, created_at 
                  FROM sms_logs 
                  ORDER BY created_at DESC 
                  LIMIT 250";
        $stmt = $db->prepare($query);
        $stmt->execute();
        $logs = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        Response::send(true, "SMS gateway logs retrieved successfully.", $logs, 200);
    } else {
        // Fetch Admin Audit logs
        $query = "SELECT a.log_id, a.user_id, a.action, a.details, a.ip_address, a.created_at,
                         u.full_name AS user_name, u.role AS user_role
                  FROM audit_logs a
                  LEFT JOIN users u ON a.user_id = u.user_id
                  ORDER BY a.created_at DESC 
                  LIMIT 250";
        $stmt = $db->prepare($query);
        $stmt->execute();
        $logs = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        Response::send(true, "Audit logs retrieved successfully.", $logs, 200);
    }
} catch (Exception $e) {
    Response::send(false, "Failed to retrieve logs: " . $e->getMessage(), null, 500);
}
?>
