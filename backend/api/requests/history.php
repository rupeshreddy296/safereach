<?php
// backend/api/requests/history.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    Response::send(false, "Method not allowed. Use GET.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$userId = $userPayload['user_id'];
$role = $userPayload['role'];

$database = new Database();
$db = $database->getConnection();

try {
    // Construct base query
    $query = "SELECT r.*, 
                     u.full_name AS requester_name,
                     du.full_name AS driver_name,
                     a.vehicle_number,
                     h.name AS hospital_name,
                     f.rating,
                     f.comments
              FROM emergency_requests r
              LEFT JOIN users u ON r.user_id = u.user_id
              LEFT JOIN ambulances a ON r.assigned_ambulance_id = a.ambulance_id
              LEFT JOIN users du ON a.driver_id = du.user_id
              LEFT JOIN hospitals h ON r.destination_hospital_id = h.hospital_id
              LEFT JOIN request_feedback f ON r.request_id = f.request_id";

    if ($role === 'user') {
        $query .= " WHERE r.user_id = ? ORDER BY r.created_at DESC";
        $stmt = $db->prepare($query);
        $stmt->execute([$userId]);
    } elseif ($role === 'driver') {
        $query .= " WHERE a.driver_id = ? ORDER BY r.created_at DESC";
        $stmt = $db->prepare($query);
        $stmt->execute([$userId]);
    } else {
        // Admin sees all
        $query .= " ORDER BY r.created_at DESC";
        $stmt = $db->prepare($query);
        $stmt->execute([]);
    }

    $rows = $stmt->fetchAll();
    $data = [];

    foreach ($rows as $row) {
        $data[] = [
            "request_id" => (int)$row['request_id'],
            "user_id" => (int)$row['user_id'],
            "patient_name" => $row['patient_name'],
            "patient_phone" => $row['patient_phone'],
            "pickup_address" => $row['pickup_address'],
            "pickup_latitude" => (double)$row['pickup_latitude'],
            "pickup_longitude" => (double)$row['pickup_longitude'],
            "requested_ambulance_type" => $row['requested_ambulance_type'],
            "emergency_type" => $row['emergency_type'],
            "description" => $row['description'],
            "assigned_ambulance_id" => $row['assigned_ambulance_id'] !== null ? (int)$row['assigned_ambulance_id'] : null,
            "destination_hospital_id" => $row['destination_hospital_id'] !== null ? (int)$row['destination_hospital_id'] : null,
            "status" => $row['status'],
            "created_at" => $row['created_at'],
            "accepted_at" => $row['accepted_at'],
            "arrived_at" => $row['arrived_at'],
            "boarded_at" => $row['boarded_at'],
            "completed_at" => $row['completed_at'],
            "cancelled_at" => $row['cancelled_at'],
            "requester_name" => $row['requester_name'],
            "driver_name" => $row['driver_name'],
            "vehicle_number" => $row['vehicle_number'],
            "hospital_name" => $row['hospital_name'],
            "rating" => $row['rating'] !== null ? (int)$row['rating'] : null,
            "comments" => $row['comments']
        ];
    }

    Response::send(true, "Request history logs retrieved.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to retrieve request history logs: " . $e->getMessage(), null, 500);
}
?>
