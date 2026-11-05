<?php
// backend/api/events/stream.php
// Real-time Event Stream (Connection Bridge between Android and Antigravity Web)

header('Content-Type: text/event-stream');
header('Cache-Control: no-cache');
header('Connection: keep-alive');
header('Access-Control-Allow-Origin: *');

require_once '../../config/db.php';

$database = new Database();
$db = $database->getConnection();

$lastId = isset($_GET['last_id']) ? (int)$_GET['last_id'] : 0;

// This script will loop and send updates whenever something changes in the DB
while (true) {
    try {
        // 1. Check for new emergency requests
        $query = "SELECT request_id, patient_name, status, created_at FROM emergency_requests WHERE request_id > ? ORDER BY request_id ASC";
        $stmt = $db->prepare($query);
        $stmt->execute([$lastId]);

        while ($row = $stmt->fetch()) {
            echo "event: new_request\n";
            echo 'data: ' . json_encode($row) . "\n\n";
            $lastId = $row['request_id'];
        }

        // 2. Check for status changes in active requests
        // (We can use a dedicated triggers table or just check timestamps)
        $statusQuery = "SELECT request_id, status, updated_at FROM emergency_requests WHERE status IN ('accepted', 'arrived', 'boarded', 'completed') AND updated_at > DATE_SUB(NOW(), INTERVAL 5 SECOND)";
        // For simplicity in this bridge, we send a 'refresh' pulse every few seconds
        // In a full production app, we would use a more specific change-log table.

        echo "event: heartbeat\n";
        echo 'data: {"time": "' . date('Y-m-d H:i:s') . '"}' . "\n\n";

        ob_flush();
        flush();

    } catch (Exception $e) {
        echo "event: error\n";
        echo 'data: {"message": "' . $e->getMessage() . '"}' . "\n\n";
        break;
    }

    // Sleep for 3 seconds before next poll
    sleep(3);
}
?>
