<?php
// backend/api/admin/hospitals.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$role = $userPayload['role'];

if ($role !== 'admin') {
    Response::send(false, "Unauthorized. Only administrators can access hospital management APIs.", null, 403);
}

$database = new Database();
$db = $database->getConnection();

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    $input = json_decode(file_get_contents("php://input"), true);
    
    $name = isset($input['name']) ? trim($input['name']) : '';
    $address = isset($input['address']) ? trim($input['address']) : '';
    $phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : null;
    $latitude = isset($input['latitude']) ? (double)$input['latitude'] : 0.0;
    $longitude = isset($input['longitude']) ? (double)$input['longitude'] : 0.0;
    $icuBeds = isset($input['available_icu_beds']) ? (int)$input['available_icu_beds'] : 0;
    $nicuBeds = isset($input['available_nicu_beds']) ? (int)$input['available_nicu_beds'] : 0;
    $status = isset($input['status']) ? strtolower(trim($input['status'])) : 'active';

    if (empty($name) || empty($address) || $latitude === 0.0 || $longitude === 0.0) {
        Response::send(false, "Missing required hospital fields: name, address, latitude, and longitude are required.", null, 400);
    }

    if (!in_array($status, ['active', 'inactive'])) {
        $status = 'active';
    }

    try {
        $query = "INSERT INTO hospitals (name, address, phone_number, latitude, longitude, available_icu_beds, available_nicu_beds, status) 
                  VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        $stmt = $db->prepare($query);
        $stmt->execute([$name, $address, $phoneNumber, $latitude, $longitude, $icuBeds, $nicuBeds, $status]);
        $hospitalId = (int)$db->lastInsertId();

        // Retrieve created hospital
        $selectQuery = "SELECT * FROM hospitals WHERE hospital_id = ? LIMIT 1";
        $selectStmt = $db->prepare($selectQuery);
        $selectStmt->execute([$hospitalId]);
        $row = $selectStmt->fetch();

        $data = [
            "hospital_id" => (int)$row['hospital_id'],
            "name" => $row['name'],
            "address" => $row['address'],
            "phone_number" => $row['phone_number'],
            "latitude" => (double)$row['latitude'],
            "longitude" => (double)$row['longitude'],
            "available_icu_beds" => (int)$row['available_icu_beds'],
            "available_nicu_beds" => (int)$row['available_nicu_beds'],
            "status" => $row['status'],
            "updated_at" => $row['updated_at']
        ];

        Response::send(true, "Hospital created successfully.", $data, 201);

    } catch (Exception $e) {
        Response::send(false, "Failed to create hospital record: " . $e->getMessage(), null, 500);
    }
} elseif ($method === 'PUT') {
    $input = json_decode(file_get_contents("php://input"), true);
    
    $hospitalId = isset($input['hospital_id']) ? (int)$input['hospital_id'] : 0;
    
    if ($hospitalId <= 0) {
        Response::send(false, "Valid hospital_id is required for update operations.", null, 400);
    }

    try {
        // Verify hospital exists
        $checkQuery = "SELECT hospital_id FROM hospitals WHERE hospital_id = ? LIMIT 1";
        $checkStmt = $db->prepare($checkQuery);
        $checkStmt->execute([$hospitalId]);
        if ($checkStmt->rowCount() === 0) {
            Response::send(false, "Hospital record not found.", null, 404);
        }

        // Dynamically build update fields
        $fields = ['name', 'address', 'phone_number', 'latitude', 'longitude', 'available_icu_beds', 'available_nicu_beds', 'status'];
        $updates = [];
        $params = [];

        foreach ($fields as $field) {
            if (isset($input[$field])) {
                $updates[] = "$field = ?";
                if ($field === 'latitude' || $field === 'longitude') {
                    $params[] = (double)$input[$field];
                } elseif ($field === 'available_icu_beds' || $field === 'available_nicu_beds') {
                    $params[] = (int)$input[$field];
                } else {
                    $params[] = trim($input[$field]);
                }
            }
        }

        if (empty($updates)) {
            Response::send(false, "No fields specified for update.", null, 400);
        }

        $params[] = $hospitalId;
        $query = "UPDATE hospitals SET " . implode(', ', $updates) . " WHERE hospital_id = ?";
        $stmt = $db->prepare($query);
        $stmt->execute($params);

        Response::send(true, "Hospital updated successfully.", null, 200);

    } catch (Exception $e) {
        Response::send(false, "Failed to update hospital record: " . $e->getMessage(), null, 500);
    }
} elseif ($method === 'DELETE') {
    $hospitalId = isset($_GET['id']) ? (int)$_GET['id'] : 0;

    if ($hospitalId <= 0) {
        Response::send(false, "Invalid hospital ID parameter.", null, 400);
    }

    try {
        // Verify hospital exists
        $checkQuery = "SELECT hospital_id FROM hospitals WHERE hospital_id = ? LIMIT 1";
        $checkStmt = $db->prepare($checkQuery);
        $checkStmt->execute([$hospitalId]);
        if ($checkStmt->rowCount() === 0) {
            Response::send(false, "Hospital record not found.", null, 404);
        }

        // Hard delete or set to inactive. Hard delete matches retro client delete query.
        $deleteQuery = "DELETE FROM hospitals WHERE hospital_id = ?";
        $deleteStmt = $db->prepare($deleteQuery);
        $deleteStmt->execute([$hospitalId]);

        Response::send(true, "Hospital record deleted successfully.", null, 200);

    } catch (Exception $e) {
        Response::send(false, "Failed to delete hospital record: " . $e->getMessage(), null, 500);
    }
} else {
    Response::send(false, "Method not allowed. Use POST, PUT, or DELETE.", null, 405);
}
?>
