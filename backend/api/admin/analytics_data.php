<?php
require_once '../../config/db.php';
require_once '../../helpers/auth.php';

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Authorization, Content-Type");

$auth = checkAuth();
if (!$auth || !in_array($auth['role'], ['admin', 'super_admin'])) {
    http_response_code(403);
    echo json_encode(["success" => false, "message" => "Unauthorized"]);
    exit;
}

try {
    // We will generate 7 days of historical booking data
    $query = "
        SELECT DATE(created_at) as date, COUNT(*) as count 
        FROM emergency_requests 
        WHERE created_at >= DATE(NOW()) - INTERVAL 6 DAY
        GROUP BY DATE(created_at)
        ORDER BY date ASC
    ";
    
    $stmt = $conn->query($query);
    $dailyBookings = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // If a day has 0 bookings it might be missing from the result. Let's fill the gaps for the last 7 days.
    $last7Days = [];
    for ($i = 6; $i >= 0; $i--) {
        $dateStr = date('Y-m-d', strtotime("-$i days"));
        $last7Days[$dateStr] = 0;
    }

    foreach ($dailyBookings as $row) {
        if (isset($last7Days[$row['date']])) {
            $last7Days[$row['date']] = (int)$row['count'];
        }
    }

    $chartData = [];
    foreach ($last7Days as $date => $count) {
        $chartData[] = [
            "name" => date('M d', strtotime($date)),
            "bookings" => $count
        ];
    }

    echo json_encode(["success" => true, "data" => $chartData]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
