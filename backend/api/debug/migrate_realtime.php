<?php
// backend/api/debug/migrate_realtime.php

require_once '../../config/db.php';
require_once '../../helpers/response.php';

$database = new Database();
$db = $database->getConnection();

try {
    // Add updated_at column to emergency_requests if it doesn't exist
    $sql = "ALTER TABLE emergency_requests ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;";
    $db->exec($sql);

    Response::send(true, "Real-time migration successful.", null, 200);
} catch (Exception $e) {
    Response::send(false, "Real-time migration failed: " . $e->getMessage(), null, 500);
}
?>
