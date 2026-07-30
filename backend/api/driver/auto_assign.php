<?php
// backend/api/driver/auto_assign.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$role = $userPayload['role'];

// Only allow dispatchers and admins
if (!in_array($role, ['super_admin', 'admin', 'dispatcher'])) {
    Response::send(false, "Forbidden. Dispatcher or administrative permissions required.", null, 403);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);
$requestId = isset($input['request_id']) ? (int)$input['request_id'] : 0;

if ($requestId <= 0) {
    Response::send(false, "Valid Request ID is required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    $db->beginTransaction();

    // 1. Fetch Request details
    $checkReq = "SELECT user_id, patient_name, pickup_address, pickup_latitude, pickup_longitude, requested_ambulance_type, assigned_ambulance_id 
                 FROM emergency_requests 
                 WHERE request_id = ? LIMIT 1 FOR UPDATE";
    $chkReqStmt = $db->prepare($checkReq);
    $chkReqStmt->execute([$requestId]);
    $request = $chkReqStmt->fetch();

    if (!$request) {
        $db->rollBack();
        Response::send(false, "Emergency request not found.", null, 404);
    }

    if ($request['assigned_ambulance_id'] !== null) {
        $db->rollBack();
        Response::send(false, "Ambulance already assigned to this request.", null, 409);
    }

    $citizenId = (int)$request['user_id'];
    $patientName = $request['patient_name'];
    $pickupAddress = $request['pickup_address'];
    $pickupLat = (double)$request['pickup_latitude'];
    $pickupLng = (double)$request['pickup_longitude'];
    $type = $request['requested_ambulance_type'];

    // 2. Query nearest online ambulance (Haversine formula)
    // Step A: Search for matching ambulance type
    $matchQuery = "SELECT ambulance_id, driver_id, vehicle_number,
                          (6371 * acos(cos(radians(?)) * cos(radians(current_latitude)) * cos(radians(current_longitude) - radians(?)) + sin(radians(?)) * sin(radians(current_latitude)))) AS distance
                   FROM ambulances
                   WHERE status = 'online' AND ambulance_type = ? AND current_latitude IS NOT NULL AND current_longitude IS NOT NULL
                   ORDER BY distance ASC LIMIT 1 FOR UPDATE";
    $matchStmt = $db->prepare($matchQuery);
    $matchStmt->execute([$pickupLat, $pickupLng, $pickupLat, $type]);
    $ambulanceRow = $matchStmt->fetch();

    // Step B: Fallback to any online ambulance if none of specific type found
    if (!$ambulanceRow) {
        $fallbackQuery = "SELECT ambulance_id, driver_id, vehicle_number,
                                 (6371 * acos(cos(radians(?)) * cos(radians(current_latitude)) * cos(radians(current_longitude) - radians(?)) + sin(radians(?)) * sin(radians(current_latitude)))) AS distance
                          FROM ambulances
                          WHERE status = 'online' AND current_latitude IS NOT NULL AND current_longitude IS NOT NULL
                          ORDER BY distance ASC LIMIT 1 FOR UPDATE";
        $fallbackStmt = $db->prepare($fallbackQuery);
        $fallbackStmt->execute([$pickupLat, $pickupLng, $pickupLat]);
        $ambulanceRow = $fallbackStmt->fetch();
    }

    if (!$ambulanceRow) {
        $db->rollBack();
        Response::send(false, "No online ambulance drivers available at this time.", null, 404);
    }

    $ambulanceId = (int)$ambulanceRow['ambulance_id'];
    $driverId = (int)$ambulanceRow['driver_id'];
    $vehicleNumber = $ambulanceRow['vehicle_number'];
    $distance = round((double)$ambulanceRow['distance'], 2);

    // 3. Update emergency request with assigned ambulance
    $updateReq = "UPDATE emergency_requests SET assigned_ambulance_id = ? WHERE request_id = ?";
    $upReqStmt = $db->prepare($updateReq);
    $upReqStmt->execute([$ambulanceId, $requestId]);

    // 4. Set ambulance status to busy
    $updateAmb = "UPDATE ambulances SET status = 'busy' WHERE ambulance_id = ?";
    $upAmbStmt = $db->prepare($updateAmb);
    $upAmbStmt->execute([$ambulanceId]);

    // 5. Initialize default patient vitals
    $checkVitals = "SELECT vitals_id FROM patient_vitals WHERE request_id = ? LIMIT 1";
    $chkVitalsStmt = $db->prepare($checkVitals);
    $chkVitalsStmt->execute([$requestId]);
    if ($chkVitalsStmt->rowCount() === 0) {
        $vitalsQuery = "INSERT INTO patient_vitals (request_id, heart_rate, blood_pressure, oxygen_saturation, temperature, patient_condition) 
                        VALUES (?, 75, '120/80', 98, 37.0, 'stable')";
        $vitalsStmt = $db->prepare($vitalsQuery);
        $vitalsStmt->execute([$requestId]);
    }

    // 6. Notifications
    // Notify Driver
    $driverNoti = "INSERT INTO notifications (user_id, title, description, type, is_read) 
                   VALUES (?, 'Emergency Assigned (Auto)', ?, 'alert', 0)";
    $driverNotiStmt = $db->prepare($driverNoti);
    $driverNotiStmt->execute([
        $driverId, 
        "Auto-routing: You have been geo-dispatched to SOS request #SR-$requestId for $patientName at $pickupAddress ($distance km away)."
    ]);

    // Notify Citizen
    $citizenNoti = "INSERT INTO notifications (user_id, title, description, type, is_read) 
                    VALUES (?, 'Ambulance Unit Geo-Dispatched', ?, 'info', 0)";
    $citizenNotiStmt = $db->prepare($citizenNoti);
    $citizenNotiStmt->execute([
        $citizenId,
        "System matched. Ambulance unit TN-$vehicleNumber has been dispatched to your location ($distance km away)."
    ]);

    // 7. Audit log
    $auditQuery = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, 'auto_dispatch', ?)";
    $auditStmt = $db->prepare($auditQuery);
    $auditStmt->execute([
        $userPayload['user_id'],
        "Auto-routed and dispatched ambulance TN-$vehicleNumber (ID: $ambulanceId) to emergency call #SR-$requestId at distance of $distance km"
    ]);

    $db->commit();

    Response::send(true, "Ambulance auto-assigned and dispatched successfully.", [
        "request_id" => $requestId,
        "assigned_ambulance_id" => $ambulanceId,
        "distance_km" => $distance
    ], 200);

} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    Response::send(false, "Auto routing allocation failed: " . $e->getMessage(), null, 500);
}
?>
