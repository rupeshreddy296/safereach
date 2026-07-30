<?php
require_once '../../config/db.php';
require_once '../../helpers/auth.php';

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Authorization, Content-Type");
header("Access-Control-Allow-Methods: GET, PUT");

$auth = checkAuth();
if (!$auth || !in_array($auth['role'], ['admin', 'super_admin'])) {
    http_response_code(403);
    echo json_encode(["success" => false, "message" => "Unauthorized"]);
    exit;
}

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    try {
        $query = "SELECT u.user_id, u.full_name, u.email, u.phone_number, u.status, u.created_at,
                         a.ambulance_id, a.plate_number
                  FROM users u
                  LEFT JOIN ambulances a ON u.user_id = a.driver_id
                  WHERE u.role = 'driver'
                  ORDER BY u.created_at DESC";

        $stmt = $conn->query($query);
        $drivers = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo json_encode(["success" => true, "data" => $drivers]);
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
} elseif ($method === 'PUT') {
    try {
        $data = json_decode(file_get_contents("php://input"));
        if (!isset($data->user_id) || !isset($data->action)) {
            throw new Exception("Missing required fields");
        }

        $userId = $data->user_id;
        $action = $data->action;

        if (in_array($action, ['active', 'inactive', 'suspended'])) {
            $stmt = $conn->prepare("UPDATE users SET status = ? WHERE user_id = ? AND role = 'driver'");
            $stmt->execute([$action, $userId]);
            echo json_encode(["success" => true, "message" => "Driver status updated to $action"]);
        } else {
            throw new Exception("Invalid action");
        }

    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
}
