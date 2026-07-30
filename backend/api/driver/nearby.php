<?php
// backend/api/driver/nearby.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    Response::send(false, "Method not allowed. Use GET.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();

$latitude = isset($_GET['latitude']) ? (double)$_GET['latitude'] : (isset($_GET['lat']) ? (double)$_GET['lat'] : 0.0);
$longitude = isset($_GET['longitude']) ? (double)$_GET['longitude'] : (isset($_GET['lng']) ? (double)$_GET['lng'] : 0.0);

if ($latitude === 0.0 || $longitude === 0.0) {
    Response::send(false, "Invalid coordinates. Latitude and longitude are required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // Select online ambulances sorted by distance using Haversine formula
    $query = "SELECT a.ambulance_id, a.vehicle_number, a.ambulance_type, a.status, a.current_latitude, a.current_longitude,
                     u.full_name AS driver_name,
                     (6371 * acos(cos(radians(?)) * cos(radians(a.current_latitude)) * cos(radians(a.current_longitude) - radians(?)) + sin(radians(?)) * sin(radians(a.current_latitude)))) AS distance
              FROM ambulances a
              INNER JOIN users u ON a.driver_id = u.user_id
              WHERE a.status = 'online' AND a.current_latitude IS NOT NULL AND a.current_longitude IS NOT NULL
              ORDER BY distance ASC";
              
    $stmt = $db->prepare($query);
    $stmt->execute([$latitude, $longitude, $latitude]);
    
    $rows = $stmt->fetchAll();
    $data = [];

    foreach ($rows as $row) {
        $distance = (double)$row['distance'];
        
        // Simple ETA calculation: distance * 1.5 minutes per km + 2 minutes base overhead
        $eta = round(($distance * 1.5) + 2, 1);

        $data[] = [
            "ambulance_id" => (int)$row['ambulance_id'],
            "driver_name" => $row['driver_name'],
            "vehicle_number" => $row['vehicle_number'],
            "ambulance_type" => $row['ambulance_type'],
            "latitude" => (double)$row['current_latitude'],
            "longitude" => (double)$row['current_longitude'],
            "distance" => round($distance, 2), // in km
            "eta" => $eta // in minutes
        ];
    }

    Response::send(true, "Nearby online ambulances retrieved.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to retrieve nearby ambulances: " . $e->getMessage(), null, 500);
}
?>
