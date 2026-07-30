<?php
// backend/api/requests/sos.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$userId = $userPayload['user_id'];

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);

$lat = isset($input['latitude']) ? (double)$input['latitude'] : 0.0;
$lng = isset($input['longitude']) ? (double)$input['longitude'] : 0.0;
$address = isset($input['address']) ? trim($input['address']) : 'Automated SOS Location';

if ($lat == 0.0 || $lng == 0.0) {
    Response::send(false, "Valid latitude and longitude required for SOS.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // Fetch user info for notifications
    $userQuery = "SELECT full_name, phone_number FROM users WHERE user_id = ? LIMIT 1";
    $userStmt = $db->prepare($userQuery);
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch();

    $db->beginTransaction();

    // 1. Create the SOS request (Always ALS - Advanced Life Support)
    $insertQuery = "INSERT INTO emergency_requests
                    (user_id, patient_name, patient_phone, pickup_address, pickup_latitude, pickup_longitude, requested_ambulance_type, emergency_type, description, status)
                    VALUES (?, ?, ?, ?, ?, ?, 'ALS', 'SOS ALERT', 'Automated SOS trigger. Immediate assistance required.', 'pending')";
    $insertStmt = $db->prepare($insertQuery);
    $insertStmt->execute([
        $userId, $user['full_name'], $user['phone_number'],
        $address, $lat, $lng
    ]);

    $requestId = (int)$db->lastInsertId();

    // 2. Alert ALL online ambulances within 10km immediately
    $radius = 10; // km
    $alertQuery = "SELECT u.user_id, a.vehicle_number,
                          (6371 * acos(cos(radians(?)) * cos(radians(current_latitude)) * cos(radians(current_longitude) - radians(?)) + sin(radians(?)) * sin(radians(current_latitude)))) AS distance
                   FROM ambulances a
                   INNER JOIN users u ON a.driver_id = u.user_id
                   WHERE a.status = 'online'
                   HAVING distance <= ?
                   ORDER BY distance ASC";
    $alertStmt = $db->prepare($alertQuery);
    $alertStmt->execute([$lat, $lng, $lat, $radius]);
    $nearbyDrivers = $alertStmt->fetchAll();

    foreach ($nearbyDrivers as $driver) {
        $driverId = (int)$driver['user_id'];
        $notiQuery = "INSERT INTO notifications (user_id, title, description, type, is_read)
                      VALUES (?, 'CRITICAL SOS ALERT', ?, 'alert', 0)";
        $desc = "URGENT: SOS triggered by " . $user['full_name'] . " at " . $address . " (" . round($driver['distance'], 2) . "km away).";
        $notiStmt = $db->prepare($notiQuery);
        $notiStmt->execute([$driverId, $desc]);
    }

    // 3. Notify Admins
    $adminQuery = "SELECT user_id FROM users WHERE role IN ('admin', 'super_admin')";
    $admins = $db->query($adminQuery)->fetchAll();
    foreach ($admins as $admin) {
        $notiQuery = "INSERT INTO notifications (user_id, title, description, type, is_read)
                      VALUES (?, 'SOS SYSTEM ALERT', ?, 'alert', 0)";
        $desc = "User " . $user['full_name'] . " has triggered an SOS at " . $address . ". Request ID: #" . $requestId;
        $notiStmt = $db->prepare($notiQuery);
        $notiStmt->execute([(int)$admin['user_id'], $desc]);
    }

    $db->commit();
    Response::send(true, "SOS triggered successfully. Emergency services are being notified.", ["request_id" => $requestId], 201);

} catch (Exception $e) {
    if ($db->inTransaction()) $db->rollBack();
    Response::send(false, "SOS failed to broadcast: " . $e->getMessage(), null, 500);
}
?>
