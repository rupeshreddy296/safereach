<?php
// backend/api/hospitals/list.php

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

$database = new Database();
$db = $database->getConnection();

try {
    if ($latitude !== 0.0 && $longitude !== 0.0) {
        // Query active hospitals sorted by distance using Haversine formula
        $query = "SELECT hospital_id, name, address, phone_number, latitude, longitude, available_icu_beds, available_nicu_beds, status, updated_at,
                         (6371 * acos(cos(radians(?)) * cos(radians(latitude)) * cos(radians(longitude) - radians(?)) + sin(radians(?)) * sin(radians(latitude)))) AS distance
                  FROM hospitals 
                  WHERE status = 'active'
                  ORDER BY distance ASC";
        $stmt = $db->prepare($query);
        $stmt->execute([$latitude, $longitude, $latitude]);
    } else {
        // Fallback if no location is provided
        $query = "SELECT hospital_id, name, address, phone_number, latitude, longitude, available_icu_beds, available_nicu_beds, status, updated_at,
                         0.0 AS distance
                  FROM hospitals 
                  WHERE status = 'active'
                  ORDER BY name ASC";
        $stmt = $db->prepare($query);
        $stmt->execute();
    }
    
    $rows = $stmt->fetchAll();
    $data = [];

    foreach ($rows as $row) {
        $data[] = [
            "hospital_id" => (int)$row['hospital_id'],
            "name" => $row['name'],
            "address" => $row['address'],
            "phone_number" => $row['phone_number'],
            "latitude" => (double)$row['latitude'],
            "longitude" => (double)$row['longitude'],
            "available_icu_beds" => (int)$row['available_icu_beds'],
            "available_nicu_beds" => (int)$row['available_nicu_beds'],
            "distance" => round((double)$row['distance'], 2), // in km
            "status" => $row['status'],
            "updated_at" => $row['updated_at']
        ];
    }

    Response::send(true, "Hospitals list retrieved.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Failed to retrieve hospitals list: " . $e->getMessage(), null, 500);
}
?>
