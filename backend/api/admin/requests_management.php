<?php
require_once '../../config/db.php';
require_once '../../helpers/auth.php';

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Authorization, Content-Type");
header("Access-Control-Allow-Methods: GET, PUT");

$auth = checkAuth();
if (!$auth || !in_array($auth['role'], ['admin', 'super_admin', 'dispatcher'])) {
    http_response_code(403);
    echo json_encode(["success" => false, "message" => "Unauthorized"]);
    exit;
}

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    try {
        $filter = $_GET['filter'] ?? 'all';
        $query = "SELECT er.*, 
                         u.full_name as patient_name, u.phone_number as patient_phone,
                         d.full_name as driver_name, d.phone_number as driver_phone,
                         h.name as hospital_name
                  FROM emergency_requests er
                  LEFT JOIN users u ON er.user_id = u.user_id
                  LEFT JOIN users d ON er.driver_id = d.user_id
                  LEFT JOIN hospitals h ON er.hospital_id = h.hospital_id";
        
        $params = [];
        if ($filter === 'active') {
            $query .= " WHERE er.status NOT IN ('completed', 'cancelled')";
        } elseif ($filter === 'sos') {
            $query .= " WHERE er.emergency_type = 'SOS'";
        } elseif ($filter === 'today') {
            $query .= " WHERE DATE(er.created_at) = CURDATE()";
        }
        
        $query .= " ORDER BY er.created_at DESC LIMIT 1000";

        $stmt = $conn->prepare($query);
        $stmt->execute($params);
        $requests = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo json_encode(["success" => true, "data" => $requests]);
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
} elseif ($method === 'PUT') {
    // Admin Override Actions
    try {
        $data = json_decode(file_get_contents("php://input"));
        if (!isset($data->request_id) || !isset($data->action)) {
            throw new Exception("Missing required fields");
        }

        $requestId = $data->request_id;
        $action = $data->action;

        if ($action === 'cancel') {
            $stmt = $conn->prepare("UPDATE emergency_requests SET status = 'cancelled' WHERE request_id = ?");
            $stmt->execute([$requestId]);
        } elseif ($action === 'assign_driver') {
            $driverId = $data->driver_id;
            $ambulanceId = $data->ambulance_id ?? null;
            $stmt = $conn->prepare("UPDATE emergency_requests SET driver_id = ?, ambulance_id = ?, status = 'driver_assigned' WHERE request_id = ?");
            $stmt->execute([$driverId, $ambulanceId, $requestId]);
        }

        echo json_encode(["success" => true, "message" => "Request updated successfully"]);
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
}
