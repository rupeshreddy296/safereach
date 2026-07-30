<?php
// backend/api/driver/location.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$userId = $userPayload['user_id'];
$role = $userPayload['role'];

if ($role !== 'driver') {
    Response::send(false, "Unauthorized. Only drivers can update telemetry location.", null, 403);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);
$latitude = isset($input['latitude']) ? (double)$input['latitude'] : 0.0;
$longitude = isset($input['longitude']) ? (double)$input['longitude'] : 0.0;

if ($latitude === 0.0 || $longitude === 0.0) {
    Response::send(false, "Invalid coordinates. Latitude and longitude must not be zero.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Check if driver has an associated ambulance
    $checkQuery = "SELECT ambulance_id, vehicle_number, ambulance_type, status FROM ambulances WHERE driver_id = ? LIMIT 1";
    $checkStmt = $db->prepare($checkQuery);
    $checkStmt->execute([$userId]);
    $ambulance = $checkStmt->fetch();

    if (!$ambulance) {
        Response::send(false, "No associated ambulance vehicle found for this driver.", null, 404);
    }

    $ambulanceId = (int)$ambulance['ambulance_id'];

    // 2. Update ambulance location
    $updateQuery = "UPDATE ambulances SET current_latitude = ?, current_longitude = ?, updated_at = CURRENT_TIMESTAMP WHERE ambulance_id = ?";
    $updateStmt = $db->prepare($updateQuery);
    $updateStmt->execute([$latitude, $longitude, $ambulanceId]);

    // 3. Log to location_history table
    $historyQuery = "INSERT INTO location_history (user_id, latitude, longitude) VALUES (?, ?, ?)";
    $historyStmt = $db->prepare($historyQuery);
    $historyStmt->execute([$userId, $latitude, $longitude]);

    // 3. Return updated vehicle data matching VehicleData model
    $data = [
        "ambulance_id" => $ambulanceId,
        "vehicle_number" => $ambulance['vehicle_number'],
        "ambulance_type" => $ambulance['ambulance_type'],
        "status" => $ambulance['status']
    ];

    Response::send(true, "Driver telemetry location updated.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to update location telemetry: " . $e->getMessage(), null, 500);
}
?>
