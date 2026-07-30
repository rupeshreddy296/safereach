<?php
// backend/api/debug/migrate.php

require_once '../../config/db.php';
require_once '../../helpers/response.php';

$database = new Database();
$db = $database->getConnection();

try {
    $sql = "ALTER TABLE emergency_requests ADD COLUMN IF NOT EXISTS scheduled_at TIMESTAMP NULL DEFAULT NULL AFTER description;";
    $db->exec($sql);

    Response::send(true, "Database migration successful.", null, 200);
} catch (Exception $e) {
    Response::send(false, "Migration failed: " . $e->getMessage(), null, 500);
}
?>
