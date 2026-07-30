<?php
// backend/api/requests/create.php

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

$patientName = isset($input['patient_name']) ? trim($input['patient_name']) : '';
$patientPhone = isset($input['patient_phone']) ? trim($input['patient_phone']) : '';
$pickupAddress = isset($input['pickup_address']) ? trim($input['pickup_address']) : '';
$pickupLatitude = isset($input['pickup_latitude']) ? (double)$input['pickup_latitude'] : 0.0;
$pickupLongitude = isset($input['pickup_longitude']) ? (double)$input['pickup_longitude'] : 0.0;
$requestedAmbulanceType = isset($input['requested_ambulance_type']) ? trim($input['requested_ambulance_type']) : 'BLS';
$emergencyType = isset($input['emergency_type']) ? trim($input['emergency_type']) : '';
$description = isset($input['description']) ? trim($input['description']) : null;

if (empty($patientName) || empty($patientPhone) || empty($pickupAddress) || empty($pickupLatitude) || empty($pickupLongitude) || empty($emergencyType)) {
    Response::send(false, "Missing required emergency fields.", null, 400);
}

// Validate ENUM ambulance type
if (!in_array($requestedAmbulanceType, ['BLS', 'ALS', 'CCU'])) {
    $requestedAmbulanceType = 'BLS';
}

$database = new Database();
$db = $database->getConnection();

try {
    $db->beginTransaction();

    // 1. Match the closest online ambulance of the requested type
    // If none are online of the requested type, search for ANY online ambulance of any type.
    $assignedAmbulanceId = null;
    $assignedDriverId = null;
    $vehicleNumber = '';

    // Step A: Search for matching ambulance type
    $matchQuery = "SELECT ambulance_id, driver_id, vehicle_number, current_latitude, current_longitude,
                          (6371 * acos(cos(radians(?)) * cos(radians(current_latitude)) * cos(radians(current_longitude) - radians(?)) + sin(radians(?)) * sin(radians(current_latitude)))) AS distance
                   FROM ambulances
                   WHERE status = 'online' AND ambulance_type = ? AND current_latitude IS NOT NULL AND current_longitude IS NOT NULL
                   ORDER BY distance ASC LIMIT 1";
    $matchStmt = $db->prepare($matchQuery);
    $matchStmt->execute([$pickupLatitude, $pickupLongitude, $pickupLatitude, $requestedAmbulanceType]);
    $ambulanceRow = $matchStmt->fetch();

    // Step B: Fallback to any online ambulance if none of specific type found
    if (!$ambulanceRow) {
        $fallbackQuery = "SELECT ambulance_id, driver_id, vehicle_number, current_latitude, current_longitude,
                                 (6371 * acos(cos(radians(?)) * cos(radians(current_latitude)) * cos(radians(current_longitude) - radians(?)) + sin(radians(?)) * sin(radians(current_latitude)))) AS distance
                          FROM ambulances
                          WHERE status = 'online' AND current_latitude IS NOT NULL AND current_longitude IS NOT NULL
                          ORDER BY distance ASC LIMIT 1";
        $fallbackStmt = $db->prepare($fallbackQuery);
        $fallbackStmt->execute([$pickupLatitude, $pickupLongitude, $pickupLatitude]);
        $ambulanceRow = $fallbackStmt->fetch();
    }

    if ($ambulanceRow) {
        $assignedAmbulanceId = (int)$ambulanceRow['ambulance_id'];
        $assignedDriverId = (int)$ambulanceRow['driver_id'];
        $vehicleNumber = $ambulanceRow['vehicle_number'];
    }

    // 2. Insert emergency request
    $insertQuery = "INSERT INTO emergency_requests 
                    (user_id, patient_name, patient_phone, pickup_address, pickup_latitude, pickup_longitude, requested_ambulance_type, emergency_type, description, assigned_ambulance_id, status) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')";
    $insertStmt = $db->prepare($insertQuery);
    $insertStmt->execute([
        $userId, $patientName, $patientPhone, $pickupAddress, 
        $pickupLatitude, $pickupLongitude, $requestedAmbulanceType, 
        $emergencyType, $description, $assignedAmbulanceId
    ]);
    
    $requestId = (int)$db->lastInsertId();

    // 3. Initialize default patient vitals if ambulance assigned
    if ($assignedAmbulanceId) {
        $vitalsQuery = "INSERT INTO patient_vitals (request_id, heart_rate, blood_pressure, oxygen_saturation, temperature, patient_condition) 
                        VALUES (?, 75, '120/80', 98, 37.0, 'stable')";
        $vitalsStmt = $db->prepare($vitalsQuery);
        $vitalsStmt->execute([$requestId]);

        // Mark ambulance status as busy (optional, but driver accepting request will make it busy.
        // Let's keep status online until accepted, or set it to busy immediately.
        // Usually, set to busy makes sense so other requests don't match it.
        $updateAmbulance = "UPDATE ambulances SET status = 'busy' WHERE ambulance_id = ?";
        $updateAmbulanceStmt = $db->prepare($updateAmbulance);
        $updateAmbulanceStmt->execute([$assignedAmbulanceId]);
    }

    // 4. Create user notification
    $notiTitle = "Emergency Request Dispatched";
    $notiDesc = $assignedAmbulanceId 
        ? "Ambulance matched. Vehicle number $vehicleNumber is on the way." 
        : "Emergency request created. We are searching for an available ambulance near you.";
    
    $notiQuery = "INSERT INTO notifications (user_id, title, description, type, is_read) VALUES (?, ?, ?, 'alert', 0)";
    $notiStmt = $db->prepare($notiQuery);
    $notiStmt->execute([$userId, $notiTitle, $notiDesc]);

    // 5. Create driver notification (if assigned)
    if ($assignedDriverId) {
        $driverNotiQuery = "INSERT INTO notifications (user_id, title, description, type, is_read) VALUES (?, ?, ?, 'alert', 0)";
        $driverNotiStmt = $db->prepare($driverNotiQuery);
        $driverNotiStmt->execute([
            $assignedDriverId, 
            "New Emergency Assigned", 
            "You have been assigned a new emergency request for patient $patientName at $pickupAddress."
        ]);
    }

    $db->commit();

    // Prepare response mapping to RequestCreateResponse
    $data = [
        "request_id" => $requestId,
        "status" => "pending",
        "requested_ambulance_type" => $requestedAmbulanceType
    ];

    Response::send(true, "Emergency request registered successfully.", $data, 201);

} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    Response::send(false, "Failed to register emergency request: " . $e->getMessage(), null, 500);
}
?>
