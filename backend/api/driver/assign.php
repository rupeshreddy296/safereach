<?php
// backend/api/driver/assign.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$role = $userPayload['role'];

// Only allow administrative and dispatcher roles to dispatch/assign units
if (!in_array($role, ['super_admin', 'admin', 'dispatcher'])) {
    Response::send(false, "Forbidden. Administrative or dispatcher privileges required.", null, 403);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);
$requestId = isset($input['request_id']) ? (int)$input['request_id'] : 0;
$ambulanceId = isset($input['ambulance_id']) ? (int)$input['ambulance_id'] : 0;
$hospitalId = isset($input['hospital_id']) && (int)$input['hospital_id'] > 0 ? (int)$input['hospital_id'] : null;

if ($requestId <= 0 || $ambulanceId <= 0) {
    Response::send(false, "Request ID and Ambulance ID are required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    $db->beginTransaction();

    // 1. Verify emergency request exists
    $checkReq = "SELECT user_id, patient_name, pickup_address, assigned_ambulance_id FROM emergency_requests WHERE request_id = ? LIMIT 1 FOR UPDATE";
    $chkReqStmt = $db->prepare($checkReq);
    $chkReqStmt->execute([$requestId]);
    $request = $chkReqStmt->fetch();

    if (!$request) {
        $db->rollBack();
        Response::send(false, "Emergency request not found.", null, 404);
    }

    $citizenId = (int)$request['user_id'];
    $patientName = $request['patient_name'];
    $pickupAddress = $request['pickup_address'];

    // If an ambulance was previously assigned, set its status back to online
    $oldAmbulanceId = $request['assigned_ambulance_id'];
    if ($oldAmbulanceId !== null && (int)$oldAmbulanceId !== $ambulanceId) {
        $resetAmb = "UPDATE ambulances SET status = 'online' WHERE ambulance_id = ?";
        $resetAmbStmt = $db->prepare($resetAmb);
        $resetAmbStmt->execute([$oldAmbulanceId]);
    }

    // 2. Verify new ambulance exists and get its driver details
    $checkAmb = "SELECT driver_id, vehicle_number, status FROM ambulances WHERE ambulance_id = ? LIMIT 1 FOR UPDATE";
    $chkAmbStmt = $db->prepare($checkAmb);
    $chkAmbStmt->execute([$ambulanceId]);
    $ambulance = $chkAmbStmt->fetch();

    if (!$ambulance) {
        $db->rollBack();
        Response::send(false, "Ambulance vehicle not found.", null, 404);
    }

    $driverId = (int)$ambulance['driver_id'];
    $vehicleNumber = $ambulance['vehicle_number'];

    // 3. Update the request
    $updateReq = "UPDATE emergency_requests 
                  SET assigned_ambulance_id = ?, destination_hospital_id = ? 
                  WHERE request_id = ?";
    $upReqStmt = $db->prepare($updateReq);
    $upReqStmt->execute([$ambulanceId, $hospitalId, $requestId]);

    // 4. Update new ambulance status to busy
    $updateAmb = "UPDATE ambulances SET status = 'busy' WHERE ambulance_id = ?";
    $upAmbStmt = $db->prepare($updateAmb);
    $upAmbStmt->execute([$ambulanceId]);

    // 5. Initialize vitals chart if not already present
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
                   VALUES (?, 'Emergency Assigned (Override)', ?, 'alert', 0)";
    $driverNotiStmt = $db->prepare($driverNoti);
    $driverNotiStmt->execute([
        $driverId, 
        "Manual Override: You have been assigned to SOS request #SR-$requestId for $patientName at $pickupAddress."
    ]);

    // Notify Citizen
    $citizenNoti = "INSERT INTO notifications (user_id, title, description, type, is_read) 
                    VALUES (?, 'Ambulance Unit Reallocated', ?, 'info', 0)";
    $citizenNotiStmt = $db->prepare($citizenNoti);
    $citizenNotiStmt->execute([
        $citizenId,
        "An ambulance unit (TN-$vehicleNumber) has been assigned to your request."
    ]);

    // 7. Audit log
    $auditQuery = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, 'manual_dispatch', ?)";
    $auditStmt = $db->prepare($auditQuery);
    $auditStmt->execute([
        $userPayload['user_id'],
        "Manually dispatched ambulance TN-$vehicleNumber (ID: $ambulanceId) to emergency call #SR-$requestId"
    ]);

    $db->commit();

    Response::send(true, "Ambulance manually assigned and dispatched successfully.", [
        "request_id" => $requestId,
        "assigned_ambulance_id" => $ambulanceId,
        "destination_hospital_id" => $hospitalId
    ], 200);

} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    Response::send(false, "Dispatch override failed: " . $e->getMessage(), null, 500);
}
?>
