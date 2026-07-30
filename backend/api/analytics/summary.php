<?php
// backend/api/analytics/summary.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$currentUser = Auth::getAuthenticatedUser();
if (!in_array($currentUser['role'], ['super_admin', 'admin', 'analytics'])) {
    Response::send(false, "Forbidden. Analytics clearance required.", null, 403);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. General counts
    $countsQuery = "SELECT 
                        (SELECT COUNT(*) FROM emergency_requests) AS total_requests,
                        (SELECT COUNT(*) FROM emergency_requests WHERE status = 'completed') AS completed_requests,
                        (SELECT COUNT(*) FROM emergency_requests WHERE status = 'pending') AS pending_requests,
                        (SELECT COUNT(*) FROM users) AS total_users,
                        (SELECT COUNT(*) FROM ambulances WHERE status = 'online') AS online_ambulances";
    $countsStmt = $db->query($countsQuery);
    $counts = $countsStmt->fetch(PDO::FETCH_ASSOC);

    // 2. Emergencies grouped by type
    $typeQuery = "SELECT emergency_type, COUNT(*) AS count 
                  FROM emergency_requests 
                  GROUP BY emergency_type 
                  ORDER BY count DESC";
    $typeStmt = $db->query($typeQuery);
    $types = $typeStmt->fetchAll(PDO::FETCH_ASSOC);

    // 3. Response times (Average in minutes between request creation and ambulance arrival)
    $responseTimeQuery = "SELECT 
                            DATE(created_at) AS date,
                            ROUND(AVG(TIMESTAMPDIFF(SECOND, created_at, arrived_at) / 60), 1) AS avg_response_time_minutes
                          FROM emergency_requests
                          WHERE arrived_at IS NOT NULL AND status != 'cancelled'
                          GROUP BY DATE(created_at)
                          ORDER BY date DESC
                          LIMIT 14";
    $responseStmt = $db->query($responseTimeQuery);
    $responseTimes = array_reverse($responseStmt->fetchAll(PDO::FETCH_ASSOC));

    // 4. Geographical coordinates for heatmap visualization
    $heatmapQuery = "SELECT pickup_latitude AS lat, pickup_longitude AS lng, COUNT(*) AS intensity 
                     FROM emergency_requests 
                     GROUP BY pickup_latitude, pickup_longitude 
                     LIMIT 500";
    $heatmapStmt = $db->query($heatmapQuery);
    $heatmaps = $heatmapStmt->fetchAll(PDO::FETCH_ASSOC);

    // 5. User registrations growth
    $growthQuery = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS count 
                    FROM users 
                    GROUP BY month 
                    ORDER BY month ASC 
                    LIMIT 12";
    $growthStmt = $db->query($growthQuery);
    $growth = $growthStmt->fetchAll(PDO::FETCH_ASSOC);

    $data = [
        "statistics" => [
            "total_requests" => (int)$counts['total_requests'],
            "completed_requests" => (int)$counts['completed_requests'],
            "pending_requests" => (int)$counts['pending_requests'],
            "total_users" => (int)$counts['total_users'],
            "online_ambulances" => (int)$counts['online_ambulances']
        ],
        "emergency_types" => $types,
        "response_time_trends" => $responseTimes,
        "growth_trends" => $growth,
        "heatmap" => $heatmaps
    ];

    Response::send(true, "Analytics calculations compiled.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to compile metrics: " . $e->getMessage(), null, 500);
}
?>
