<?php
// backend/api/admin/ambulances.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$currentUser = Auth::getAuthenticatedUser();
if (!in_array($currentUser['role'], ['super_admin', 'admin'])) {
    Response::send(false, "Forbidden. Administrative access required.", null, 403);
}

$method = $_SERVER['REQUEST_METHOD'];
$database = new Database();
$db = $database->getConnection();

try {
    switch ($method) {
        case 'GET':
            // Read Ambulances List
            $query = "SELECT a.ambulance_id, a.driver_id, a.vehicle_number, a.ambulance_type, a.status, a.current_latitude, a.current_longitude, a.updated_at,
                             u.full_name AS driver_name, u.phone_number AS driver_phone
                      FROM ambulances a
                      LEFT JOIN users u ON a.driver_id = u.user_id
                      ORDER BY a.ambulance_id DESC";
            
            $stmt = $db->prepare($query);
            $stmt->execute();
            $ambulances = $stmt->fetchAll(PDO::FETCH_ASSOC);
            
            Response::send(true, "Ambulance units list retrieved successfully.", $ambulances, 200);
            break;

        case 'POST':
            // Create Ambulance
            $input = json_decode(file_get_contents("php://input"), true);
            $driverId = isset($input['driver_id']) ? (int)$input['driver_id'] : 0;
            $vehicleNumber = isset($input['vehicle_number']) ? trim($input['vehicle_number']) : '';
            $ambulanceType = isset($input['ambulance_type']) ? trim($input['ambulance_type']) : 'BLS';
            $status = isset($input['status']) ? trim($input['status']) : 'offline';

            if (empty($vehicleNumber) || $driverId <= 0) {
                Response::send(false, "Driver ID and Vehicle Number are required.", null, 400);
            }

            // Verify unique vehicle
            $dupQuery = "SELECT ambulance_id FROM ambulances WHERE vehicle_number = ? OR driver_id = ? LIMIT 1";
            $dupStmt = $db->prepare($dupQuery);
            $dupStmt->execute([$vehicleNumber, $driverId]);
            if ($dupStmt->fetch()) {
                Response::send(false, "Vehicle number is already registered, or driver is already mapped to another ambulance.", null, 409);
            }

            $insertQuery = "INSERT INTO ambulances (driver_id, vehicle_number, ambulance_type, status, current_latitude, current_longitude)
                            VALUES (?, ?, ?, ?, 13.0232, 80.0175)"; // Default coordinates set to Chennai
            $insertStmt = $db->prepare($insertQuery);
            $insertStmt->execute([$driverId, $vehicleNumber, $ambulanceType, $status]);
            $newId = $db->lastInsertId();

            // Log action in audit logs
            $logQuery = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, 'create_ambulance', ?)";
            $logStmt = $db->prepare($logQuery);
            $logStmt->execute([$currentUser['user_id'], "Registered ambulance vehicle ID $newId ($vehicleNumber)"]);

            Response::send(true, "Ambulance registered successfully.", ["ambulance_id" => (int)$newId], 201);
            break;

        case 'PUT':
            // Update Ambulance details
            $input = json_decode(file_get_contents("php://input"), true);
            $ambulanceId = isset($input['ambulance_id']) ? (int)$input['ambulance_id'] : 0;
            $driverId = isset($input['driver_id']) ? (int)$input['driver_id'] : 0;
            $vehicleNumber = isset($input['vehicle_number']) ? trim($input['vehicle_number']) : '';
            $ambulanceType = isset($input['ambulance_type']) ? trim($input['ambulance_type']) : 'BLS';
            $status = isset($input['status']) ? trim($input['status']) : 'offline';

            if ($ambulanceId <= 0 || empty($vehicleNumber) || $driverId <= 0) {
                Response::send(false, "Required validation fields are missing.", null, 400);
            }

            // Verify unique constraints
            $dupQuery = "SELECT ambulance_id FROM ambulances WHERE (vehicle_number = ? OR driver_id = ?) AND ambulance_id != ? LIMIT 1";
            $dupStmt = $db->prepare($dupQuery);
            $dupStmt->execute([$vehicleNumber, $driverId, $ambulanceId]);
            if ($dupStmt->fetch()) {
                Response::send(false, "Vehicle number already exists, or the driver is already assigned to another ambulance.", null, 409);
            }

            $updateQuery = "UPDATE ambulances 
                            SET driver_id = ?, vehicle_number = ?, ambulance_type = ?, status = ?
                            WHERE ambulance_id = ?";
            $updateStmt = $db->prepare($updateQuery);
            $updateStmt->execute([$driverId, $vehicleNumber, $ambulanceType, $status, $ambulanceId]);

            // Log action in audit logs
            $logQuery = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, 'update_ambulance', ?)";
            $logStmt = $db->prepare($logQuery);
            $logStmt->execute([$currentUser['user_id'], "Updated ambulance vehicle ID $ambulanceId"]);

            Response::send(true, "Ambulance updated successfully.", null, 200);
            break;

        case 'DELETE':
            // Delete Ambulance
            $ambulanceId = isset($_GET['id']) ? (int)$_GET['id'] : 0;
            if ($ambulanceId <= 0) {
                Response::send(false, "Valid Ambulance ID query parameter is required.", null, 400);
            }

            $deleteQuery = "DELETE FROM ambulances WHERE ambulance_id = ?";
            $deleteStmt = $db->prepare($deleteQuery);
            $deleteStmt->execute([$ambulanceId]);

            // Log action in audit logs
            $logQuery = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, 'delete_ambulance', ?)";
            $logStmt = $db->prepare($logQuery);
            $logStmt->execute([$currentUser['user_id'], "Deleted ambulance ID $ambulanceId"]);

            Response::send(true, "Ambulance deleted successfully.", null, 200);
            break;

        default:
            Response::send(false, "Method not allowed. Use GET, POST, PUT, or DELETE.", null, 405);
    }
} catch (Exception $e) {
    Response::send(false, "Database operation failed: " . $e->getMessage(), null, 500);
}
?>
