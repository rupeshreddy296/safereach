<?php
// backend/api/user/addresses.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$userId = $userPayload['user_id'];

$database = new Database();
$db = $database->getConnection();

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    try {
        $query = "SELECT address_id, title, subtitle, address, latitude, longitude, created_at 
                  FROM saved_addresses 
                  WHERE user_id = ? 
                  ORDER BY created_at DESC";
        $stmt = $db->prepare($query);
        $stmt->execute([$userId]);
        $rows = $stmt->fetchAll();

        $data = [];
        foreach ($rows as $row) {
            $data[] = [
                "address_id" => (int)$row['address_id'],
                "title" => $row['title'],
                "subtitle" => $row['subtitle'],
                "address" => $row['address'],
                "latitude" => (double)$row['latitude'],
                "longitude" => (double)$row['longitude'],
                "created_at" => $row['created_at']
            ];
        }

        Response::send(true, "Saved addresses retrieved.", $data, 200);

    } catch (Exception $e) {
        Response::send(false, "Failed to retrieve addresses: " . $e->getMessage(), null, 500);
    }
} elseif ($method === 'POST') {
    // Get raw JSON input
    $input = json_decode(file_get_contents("php://input"), true);

    $title = isset($input['title']) ? trim($input['title']) : '';
    $subtitle = isset($input['subtitle']) ? trim($input['subtitle']) : null;
    $address = isset($input['address']) ? trim($input['address']) : '';
    $latitude = isset($input['latitude']) ? (double)$input['latitude'] : 0.0;
    $longitude = isset($input['longitude']) ? (double)$input['longitude'] : 0.0;

    if (empty($title) || empty($address) || empty($latitude) || empty($longitude)) {
        Response::send(false, "Missing required fields: title, address, latitude, and longitude are required.", null, 400);
    }

    try {
        $insertQuery = "INSERT INTO saved_addresses (user_id, title, subtitle, address, latitude, longitude) 
                        VALUES (?, ?, ?, ?, ?, ?)";
        $stmt = $db->prepare($insertQuery);
        $stmt->execute([$userId, $title, $subtitle, $address, $latitude, $longitude]);
        $addressId = (int)$db->lastInsertId();

        // Retrieve inserted address
        $selectQuery = "SELECT address_id, title, subtitle, address, latitude, longitude, created_at 
                        FROM saved_addresses 
                        WHERE address_id = ? LIMIT 1";
        $selectStmt = $db->prepare($selectQuery);
        $selectStmt->execute([$addressId]);
        $row = $selectStmt->fetch();

        $data = [
            "address_id" => (int)$row['address_id'],
            "title" => $row['title'],
            "subtitle" => $row['subtitle'],
            "address" => $row['address'],
            "latitude" => (double)$row['latitude'],
            "longitude" => (double)$row['longitude'],
            "created_at" => $row['created_at']
        ];

        Response::send(true, "Address saved successfully.", $data, 201);

    } catch (Exception $e) {
        Response::send(false, "Failed to save address: " . $e->getMessage(), null, 500);
    }
} elseif ($method === 'DELETE') {
    $addressId = isset($_GET['id']) ? (int)$_GET['id'] : 0;

    if ($addressId <= 0) {
        Response::send(false, "Invalid address ID parameter.", null, 400);
    }

    try {
        // Verify ownership before deleting
        $checkQuery = "SELECT address_id FROM saved_addresses WHERE address_id = ? AND user_id = ? LIMIT 1";
        $checkStmt = $db->prepare($checkQuery);
        $checkStmt->execute([$addressId, $userId]);
        
        if ($checkStmt->rowCount() === 0) {
            Response::send(false, "Address not found or unauthorized to delete.", null, 404);
        }

        $deleteQuery = "DELETE FROM saved_addresses WHERE address_id = ?";
        $deleteStmt = $db->prepare($deleteQuery);
        $deleteStmt->execute([$addressId]);

        Response::send(true, "Address deleted successfully.", null, 200);

    } catch (Exception $e) {
        Response::send(false, "Failed to delete address: " . $e->getMessage(), null, 500);
    }
} else {
    Response::send(false, "Method not allowed. Use GET, POST, or DELETE.", null, 405);
}
?>
