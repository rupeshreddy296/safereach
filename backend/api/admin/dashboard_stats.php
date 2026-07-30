<?php
require_once '../../config/db.php';
require_once '../../helpers/auth.php';

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Authorization, Content-Type");

$auth = checkAuth();
if (!$auth || !in_array($auth['role'], ['admin', 'super_admin', 'dispatcher'])) {
    http_response_code(403);
    echo json_encode(["success" => false, "message" => "Unauthorized"]);
    exit;
}

try {
    $stats = [
        "total_citizens" => 0,
        "total_drivers" => 0,
        "total_ambulances" => 0,
        "available_ambulances" => 0,
        "busy_ambulances" => 0,
        "offline_ambulances" => 0,
        "maintenance_ambulances" => 0,
        "total_hospitals" => 0,
        "today_bookings" => 0,
        "active_emergencies" => 0,
        "completed_trips" => 0,
        "cancelled_trips" => 0,
        "sos_requests" => 0,
        "active_trips" => 0
    ];

    // Total Citizens
    $stmt = $conn->query("SELECT COUNT(*) FROM users WHERE role = 'user'");
    $stats["total_citizens"] = (int)$stmt->fetchColumn();

    // Total Drivers
    $stmt = $conn->query("SELECT COUNT(*) FROM users WHERE role = 'driver'");
    $stats["total_drivers"] = (int)$stmt->fetchColumn();

    // Ambulances
    $stmt = $conn->query("SELECT COUNT(*) as total, 
                          SUM(CASE WHEN status = 'available' THEN 1 ELSE 0 END) as available,
                          SUM(CASE WHEN status = 'busy' THEN 1 ELSE 0 END) as busy,
                          SUM(CASE WHEN status = 'offline' THEN 1 ELSE 0 END) as offline,
                          SUM(CASE WHEN status = 'maintenance' THEN 1 ELSE 0 END) as maintenance
                          FROM ambulances");
    $amb = $stmt->fetch(PDO::FETCH_ASSOC);
    $stats["total_ambulances"] = (int)($amb['total'] ?? 0);
    $stats["available_ambulances"] = (int)($amb['available'] ?? 0);
    $stats["busy_ambulances"] = (int)($amb['busy'] ?? 0);
    $stats["offline_ambulances"] = (int)($amb['offline'] ?? 0);
    $stats["maintenance_ambulances"] = (int)($amb['maintenance'] ?? 0);

    // Total Hospitals
    $stmt = $conn->query("SELECT COUNT(*) FROM hospitals");
    $stats["total_hospitals"] = (int)$stmt->fetchColumn();

    // Today's Bookings
    $stmt = $conn->query("SELECT COUNT(*) FROM emergency_requests WHERE DATE(created_at) = CURDATE()");
    $stats["today_bookings"] = (int)$stmt->fetchColumn();

    // Active Emergencies
    $stmt = $conn->query("SELECT COUNT(*) FROM emergency_requests WHERE status IN ('pending', 'accepted', 'driver_assigned', 'driver_arriving', 'patient_picked_up')");
    $stats["active_emergencies"] = (int)$stmt->fetchColumn();

    // Completed / Cancelled Trips
    $stmt = $conn->query("SELECT COUNT(*) FROM emergency_requests WHERE status = 'completed'");
    $stats["completed_trips"] = (int)$stmt->fetchColumn();

    $stmt = $conn->query("SELECT COUNT(*) FROM emergency_requests WHERE status = 'cancelled'");
    $stats["cancelled_trips"] = (int)$stmt->fetchColumn();

    // SOS Requests
    $stmt = $conn->query("SELECT COUNT(*) FROM emergency_requests WHERE emergency_type = 'SOS'");
    $stats["sos_requests"] = (int)$stmt->fetchColumn();

    // Active Trips (Driver assigned and moving)
    $stmt = $conn->query("SELECT COUNT(*) FROM emergency_requests WHERE status IN ('driver_assigned', 'driver_arriving', 'patient_picked_up')");
    $stats["active_trips"] = (int)$stmt->fetchColumn();

    echo json_encode(["success" => true, "data" => $stats]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Server Error: " . $e->getMessage()]);
}
