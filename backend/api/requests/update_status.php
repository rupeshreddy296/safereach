<?php
// backend/api/requests/update_status.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$authUserId = $userPayload['user_id'];
$authUserRole = $userPayload['role'];

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);

$requestId = isset($input['request_id']) ? (int)$input['request_id'] : 0;
$status = isset($input['status']) ? strtolower(trim($input['status'])) : '';
$destinationHospitalId = isset($input['destination_hospital_id']) ? (int)$input['destination_hospital_id'] : null;

if ($requestId <= 0 || empty($status)) {
    Response::send(false, "Request ID and status are required fields.", null, 400);
}

$validStatuses = ['pending', 'accepted', 'arrived', 'boarded', 'completed', 'cancelled'];
if (!in_array($status, $validStatuses)) {
    Response::send(false, "Invalid status transition value.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Fetch current request details
    $query = "SELECT user_id, assigned_ambulance_id, status FROM emergency_requests WHERE request_id = ? LIMIT 1";
    $stmt = $db->prepare($query);
    $stmt->execute([$requestId]);
    $request = $stmt->fetch();

    if (!$request) {
        Response::send(false, "Emergency request not found.", null, 404);
    }

    $citizenId = (int)$request['user_id'];
    $ambulanceId = $request['assigned_ambulance_id'] !== null ? (int)$request['assigned_ambulance_id'] : null;

    // Security check: Citizen can only cancel. Driver can transition accepted, arrived, boarded, completed. Admin can do all.
    if ($authUserRole === 'user' && $authUserId === $citizenId) {
        if ($status !== 'cancelled') {
            Response::send(false, "Unauthorized status transition. Citizens can only cancel requests.", null, 403);
        }
    }

    $db->beginTransaction();

    // 2. Identify the field to update based on the status transition
    $timestampField = null;
    if ($status === 'accepted') {
        $timestampField = 'accepted_at';
    } elseif ($status === 'arrived') {
        $timestampField = 'arrived_at';
    } elseif ($status === 'boarded') {
        $timestampField = 'boarded_at';
    } elseif ($status === 'completed') {
        $timestampField = 'completed_at';
    } elseif ($status === 'cancelled') {
        $timestampField = 'cancelled_at';
    }

    // 3. Update emergency request
    $updateFields = ["status = ?"];
    $updateParams = [$status];

    if ($timestampField) {
        $updateFields[] = "$timestampField = CURRENT_TIMESTAMP";
    }

    if ($status === 'boarded' && $destinationHospitalId !== null && $destinationHospitalId > 0) {
        $updateFields[] = "destination_hospital_id = ?";
        $updateParams[] = $destinationHospitalId;
    }

    $updateParams[] = $requestId; // for WHERE clause
    
    $updateQuery = "UPDATE emergency_requests SET " . implode(', ', $updateFields) . " WHERE request_id = ?";
    $updateStmt = $db->prepare($updateQuery);
    $updateStmt->execute($updateParams);

    // 4. Update ambulance availability status back to 'online' if completed or cancelled
    if (($status === 'completed' || $status === 'cancelled') && $ambulanceId) {
        $updateAmbulance = "UPDATE ambulances SET status = 'online' WHERE ambulance_id = ?";
        $updateAmbulanceStmt = $db->prepare($updateAmbulance);
        $updateAmbulanceStmt->execute([$ambulanceId]);
    }

    // 5. Trigger notifications & logs
    $notiTitle = "Request Status Updated";
    $notiDesc = "Your request status is now: " . ucfirst($status) . ".";

    if ($status === 'accepted') {
        $notiDesc = "An ambulance has accepted your emergency request and is heading towards you.";
    } elseif ($status === 'arrived') {
        $notiDesc = "The ambulance has arrived at your pickup location.";
    } elseif ($status === 'boarded') {
        $notiDesc = "The patient has boarded the ambulance. Transit initiated.";
    } elseif ($status === 'completed') {
        $notiDesc = "The trip is completed. Patient has been safely admitted to the hospital.";
    } elseif ($status === 'cancelled') {
        $notiDesc = "The emergency booking has been cancelled.";
    }

    // Notify Citizen
    $notiQuery = "INSERT INTO notifications (user_id, title, description, type, is_read) VALUES (?, ?, ?, 'info', 0)";
    $notiStmt = $db->prepare($notiQuery);
    $notiStmt->execute([$citizenId, $notiTitle, $notiDesc]);

    // Notify Driver if citizen cancels
    if ($status === 'cancelled' && $ambulanceId) {
        // Fetch driver user_id
        $driverQuery = "SELECT driver_id FROM ambulances WHERE ambulance_id = ? LIMIT 1";
        $driverStmt = $db->prepare($driverQuery);
        $driverStmt->execute([$ambulanceId]);
        $driverRow = $driverStmt->fetch();
        if ($driverRow) {
            $driverId = (int)$driverRow['driver_id'];
            $driverNotiQuery = "INSERT INTO notifications (user_id, title, description, type, is_read) VALUES (?, ?, ?, 'alert', 0)";
            $driverNotiStmt = $db->prepare($driverNotiQuery);
            $driverNotiStmt->execute([$driverId, "Emergency Cancelled", "The active request assigned to you has been cancelled by the patient."]);
        }
    }

    $db->commit();

    // Format response matching StatusUpdateRequest
    $data = [
        "request_id" => $requestId,
        "status" => $status,
        "destination_hospital_id" => $destinationHospitalId
    ];

    Response::send(true, "Request status updated successfully.", $data, 200);

} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    Response::send(false, "Failed to update request status: " . $e->getMessage(), null, 500);
}
?>
