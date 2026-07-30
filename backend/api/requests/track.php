<?php
// backend/api/requests/track.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    Response::send(false, "Method not allowed. Use GET.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();

$requestId = isset($_GET['request_id']) ? (int)$_GET['request_id'] : 0;

if ($requestId <= 0) {
    Response::send(false, "Valid request_id query parameter is required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Fetch the aggregated request details
    $query = "SELECT r.*, 
                     u.full_name AS requester_name, u.phone_number AS requester_phone,
                     a.ambulance_id, a.vehicle_number, a.ambulance_type, a.current_latitude AS ambulance_lat, a.current_longitude AS ambulance_lng,
                     du.full_name AS driver_name, du.phone_number AS driver_phone,
                     h.hospital_id, h.name AS hospital_name, h.address AS hospital_address, h.latitude AS hospital_lat, h.longitude AS hospital_lng,
                     v.heart_rate, v.blood_pressure, v.oxygen_saturation, v.temperature, v.patient_condition, v.updated_at AS vitals_updated
              FROM emergency_requests r
              LEFT JOIN users u ON r.user_id = u.user_id
              LEFT JOIN ambulances a ON r.assigned_ambulance_id = a.ambulance_id
              LEFT JOIN users du ON a.driver_id = du.user_id
              LEFT JOIN hospitals h ON r.destination_hospital_id = h.hospital_id
              LEFT JOIN patient_vitals v ON r.request_id = v.request_id
              WHERE r.request_id = ? 
              LIMIT 1";
              
    $stmt = $db->prepare($query);
    $stmt->execute([$requestId]);
    $row = $stmt->fetch();

    if (!$row) {
        Response::send(false, "Emergency request not found.", null, 404);
    }

    // 2. Format response to match TrackResponse structure
    
    // Patient Info
    $patient = [
        "name" => $row['patient_name'],
        "phone" => $row['patient_phone']
    ];

    // Pickup Location
    $pickup = [
        "address" => $row['pickup_address'],
        "latitude" => (double)$row['pickup_latitude'],
        "longitude" => (double)$row['pickup_longitude']
    ];

    // Emergency Info
    $emergency = [
        "type" => $row['emergency_type'],
        "description" => $row['description'],
        "requested_ambulance_type" => $row['requested_ambulance_type']
    ];

    // Timestamps
    $timestamps = [
        "created" => $row['created_at'],
        "accepted" => $row['accepted_at'],
        "arrived" => $row['arrived_at'],
        "boarded" => $row['boarded_at'],
        "completed" => $row['completed_at'],
        "cancelled" => $row['cancelled_at']
    ];

    // Assigned Ambulance Info
    $assignedAmbulance = null;
    if ($row['assigned_ambulance_id'] !== null) {
        $assignedAmbulance = [
            "ambulance_id" => (int)$row['assigned_ambulance_id'],
            "vehicle_number" => $row['vehicle_number'],
            "type" => $row['ambulance_type'],
            "driver_name" => $row['driver_name'] ?? 'Driver',
            "driver_phone" => $row['driver_phone'] ?? '',
            "latitude" => $row['ambulance_lat'] !== null ? (double)$row['ambulance_lat'] : null,
            "longitude" => $row['ambulance_lng'] !== null ? (double)$row['ambulance_lng'] : null
        ];
    }

    // Destination Hospital Info
    $destinationHospital = null;
    if ($row['destination_hospital_id'] !== null) {
        $destinationHospital = [
            "hospital_id" => (int)$row['destination_hospital_id'],
            "name" => $row['hospital_name'],
            "address" => $row['hospital_address'],
            "latitude" => (double)$row['hospital_lat'],
            "longitude" => (double)$row['hospital_lng']
        ];
    }

    // Vitals Info
    $vitals = null;
    if ($row['heart_rate'] !== null) {
        $vitals = [
            "heart_rate" => (int)$row['heart_rate'],
            "blood_pressure" => $row['blood_pressure'],
            "oxygen_saturation" => (int)$row['oxygen_saturation'],
            "temperature" => (double)$row['temperature'],
            "condition" => $row['patient_condition'],
            "updated_at" => $row['vitals_updated']
        ];
    }

    $data = [
        "request_id" => (int)$row['request_id'],
        "status" => $row['status'],
        "patient" => $patient,
        "pickup" => $pickup,
        "emergency" => $emergency,
        "timestamps" => $timestamps,
        "assigned_ambulance" => $assignedAmbulance,
        "destination_hospital" => $destinationHospital,
        "vitals" => $vitals
    ];

    Response::send(true, "Tracking details retrieved.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to retrieve tracking details: " . $e->getMessage(), null, 500);
}
?>
