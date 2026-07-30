<?php
// backend/api/driver/status.php

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
    Response::send(false, "Unauthorized. Only drivers can update driver status.", null, 403);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);
$status = isset($input['status']) ? strtolower(trim($input['status'])) : '';

if ($status === 'available') {
    $status = 'online';
}

if (empty($status) || !in_array($status, ['online', 'busy', 'offline'])) {
    Response::send(false, "Invalid status. Status must be: online, busy, or offline.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Check if driver has an associated ambulance
    $checkQuery = "SELECT ambulance_id, vehicle_number, ambulance_type FROM ambulances WHERE driver_id = ? LIMIT 1";
    $checkStmt = $db->prepare($checkQuery);
    $checkStmt->execute([$userId]);
    $ambulance = $checkStmt->fetch();

    if (!$ambulance) {
        Response::send(false, "No associated ambulance vehicle found for this driver.", null, 404);
    }

    $ambulanceId = (int)$ambulance['ambulance_id'];

    // 2. Update ambulance status
    $updateQuery = "UPDATE ambulances SET status = ? WHERE ambulance_id = ?";
    $updateStmt = $db->prepare($updateQuery);
    $updateStmt->execute([$status, $ambulanceId]);

    // 3. Return updated vehicle data matching VehicleData model
    $data = [
        "ambulance_id" => $ambulanceId,
        "vehicle_number" => $ambulance['vehicle_number'],
        "ambulance_type" => $ambulance['ambulance_type'],
        "status" => $status
    ];

    Response::send(true, "Driver ambulance status updated.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to update status: " . $e->getMessage(), null, 500);
}
?>
