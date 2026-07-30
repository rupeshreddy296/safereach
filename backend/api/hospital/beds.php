<?php
// backend/api/hospital/beds.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$currentUser = Auth::getAuthenticatedUser();
$role = $currentUser['role'];

// Only allow hospital administrative roles
if (!in_array($role, ['super_admin', 'admin', 'hospital', 'reception'])) {
    Response::send(false, "Forbidden. Hospital management credentials required.", null, 403);
}

$method = $_SERVER['REQUEST_METHOD'];
$database = new Database();
$db = $database->getConnection();

try {
    // 1. Resolve Hospital ID
    $hospitalId = 0;
    if (in_array($role, ['super_admin', 'admin'])) {
        $hospitalId = isset($_GET['hospital_id']) ? (int)$_GET['hospital_id'] : 0;
    } else {
        $hospitalId = isset($currentUser['hospital_id']) ? (int)$currentUser['hospital_id'] : 0;
    }

    if ($hospitalId <= 0) {
        Response::send(false, "No linked hospital facility identified.", null, 400);
    }

    switch ($method) {
        case 'GET':
            // Fetch bed counts
            $query = "SELECT hospital_id, name, available_icu_beds, available_nicu_beds, available_general_beds, status 
                      FROM hospitals 
                      WHERE hospital_id = ? 
                      LIMIT 1";
            $stmt = $db->prepare($query);
            $stmt->execute([$hospitalId]);
            $hospital = $stmt->fetch(PDO::FETCH_ASSOC);

            if (!$hospital) {
                Response::send(false, "Hospital facility details not found.", null, 404);
            }

            Response::send(true, "Bed capacity retrieved successfully.", $hospital, 200);
            break;

        case 'POST':
        case 'PUT':
            // Update bed counts
            $input = json_decode(file_get_contents("php://input"), true);
            $icu = isset($input['available_icu_beds']) ? (int)$input['available_icu_beds'] : -1;
            $nicu = isset($input['available_nicu_beds']) ? (int)$input['available_nicu_beds'] : -1;
            $general = isset($input['available_general_beds']) ? (int)$input['available_general_beds'] : -1;

            if ($icu < 0 || $nicu < 0 || $general < 0) {
                Response::send(false, "Beds counts cannot be negative numbers.", null, 400);
            }

            $updateQuery = "UPDATE hospitals 
                            SET available_icu_beds = ?, available_nicu_beds = ?, available_general_beds = ?
                            WHERE hospital_id = ?";
            $updateStmt = $db->prepare($updateQuery);
            $updateStmt->execute([$icu, $nicu, $general, $hospitalId]);

            // Log action in audit logs
            $logQuery = "INSERT INTO audit_logs (user_id, action, details) VALUES (?, 'update_beds', ?)";
            $logStmt = $db->prepare($logQuery);
            $logStmt->execute([$currentUser['user_id'], "Updated beds inventory for hospital ID $hospitalId: ICU=$icu, NICU=$nicu, General=$general"]);

            Response::send(true, "Bed capacity updated successfully.", null, 200);
            break;

        default:
            Response::send(false, "Method not allowed. Use GET or POST.", null, 405);
    }
} catch (Exception $e) {
    Response::send(false, "Operation failed: " . $e->getMessage(), null, 500);
}
?>
