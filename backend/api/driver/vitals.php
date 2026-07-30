<?php
// backend/api/driver/vitals.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$role = $userPayload['role'];

if ($role !== 'driver' && $role !== 'admin') {
    Response::send(false, "Unauthorized. Only drivers or admins can update patient vitals.", null, 403);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);

$requestId = isset($input['request_id']) ? (int)$input['request_id'] : 0;
$heartRate = isset($input['heart_rate']) ? (int)$input['heart_rate'] : null;
$bloodPressure = isset($input['blood_pressure']) ? trim($input['blood_pressure']) : null;
$oxygenSaturation = isset($input['oxygen_saturation']) ? (int)$input['oxygen_saturation'] : null;
$temperature = isset($input['temperature']) ? (double)$input['temperature'] : null;
$patientCondition = isset($input['patient_condition']) ? strtolower(trim($input['patient_condition'])) : 'stable';

if ($requestId <= 0) {
    Response::send(false, "Valid request_id is required.", null, 400);
}

// Validate patient condition ENUM
if (!in_array($patientCondition, ['stable', 'critical', 'unconscious', 'improving'])) {
    $patientCondition = 'stable';
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Verify emergency request exists
    $checkRequest = "SELECT request_id FROM emergency_requests WHERE request_id = ? LIMIT 1";
    $checkStmt = $db->prepare($checkRequest);
    $checkStmt->execute([$requestId]);
    if ($checkStmt->rowCount() === 0) {
        Response::send(false, "Emergency request not found.", null, 404);
    }

    // 2. Insert or update patient vitals
    $query = "INSERT INTO patient_vitals (request_id, heart_rate, blood_pressure, oxygen_saturation, temperature, patient_condition) 
              VALUES (?, ?, ?, ?, ?, ?)
              ON DUPLICATE KEY UPDATE 
                heart_rate = VALUES(heart_rate), 
                blood_pressure = VALUES(blood_pressure), 
                oxygen_saturation = VALUES(oxygen_saturation), 
                temperature = VALUES(temperature), 
                patient_condition = VALUES(patient_condition)";
                
    $stmt = $db->prepare($query);
    $stmt->execute([$requestId, $heartRate, $bloodPressure, $oxygenSaturation, $temperature, $patientCondition]);

    // 3. Return updated VitalsUpdateRequest payload
    $data = [
        "request_id" => $requestId,
        "heart_rate" => $heartRate,
        "blood_pressure" => $bloodPressure,
        "oxygen_saturation" => $oxygenSaturation,
        "temperature" => $temperature,
        "patient_condition" => $patientCondition
    ];

    Response::send(true, "Patient vitals updated successfully.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to update patient vitals: " . $e->getMessage(), null, 500);
}
?>
