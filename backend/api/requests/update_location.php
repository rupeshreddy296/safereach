<?php
// backend/api/requests/update_location.php

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
$latitude = isset($input['latitude']) ? (double)$input['latitude'] : 0.0;
$longitude = isset($input['longitude']) ? (double)$input['longitude'] : 0.0;

if ($requestId <= 0 || $latitude === 0.0 || $longitude === 0.0) {
    Response::send(false, "Request ID, latitude and longitude are required.", null, 400);
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

    // Security check: Only the citizen who made the request, their assigned driver, or admin can update the location.
    $isDriver = false;
    if ($authUserRole === 'driver' && $ambulanceId) {
        $driverQuery = "SELECT driver_id FROM ambulances WHERE ambulance_id = ? LIMIT 1";
        $driverStmt = $db->prepare($driverQuery);
        $driverStmt->execute([$ambulanceId]);
        $driverRow = $driverStmt->fetch();
        if ($driverRow && (int)$driverRow['driver_id'] === $authUserId) {
            $isDriver = true;
        }
    }

    $isAdmin = in_array($authUserRole, ['admin', 'super_admin', 'dispatcher']);

    if (($authUserId !== $citizenId) && !$isDriver && !$isAdmin) {
        Response::send(false, "Unauthorized. You cannot update location for this request.", null, 403);
    }

    // 2. Update the pickup location coordinates
    $updateQuery = "UPDATE emergency_requests SET pickup_latitude = ?, pickup_longitude = ? WHERE request_id = ?";
    $updateStmt = $db->prepare($updateQuery);
    $updateStmt->execute([$latitude, $longitude, $requestId]);

    $data = [
        "request_id" => $requestId,
        "latitude" => $latitude,
        "longitude" => $longitude
    ];

    Response::send(true, "Request pickup location updated successfully.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to update request location: " . $e->getMessage(), null, 500);
}
?>
