<?php
// backend/api/auth/login.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';
require_once '../../helpers/sms.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);

// Extract parameters
$identifier = isset($input['email']) ? trim($input['email']) : (isset($input['phone']) ? trim($input['phone']) : '');
$password = isset($input['password']) ? trim($input['password']) : '';
$role     = isset($input['role'])     ? strtolower(trim($input['role'])) : '';

if (empty($identifier) || empty($password)) {
    Response::send(false, "Email/Phone and password are required.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // Select by email/phone+role when role is specified (same email can exist per role)
    if (!empty($role) && in_array($role, ['user', 'driver', 'admin'])) {
        if ($role === 'admin') {
            $query = "SELECT user_id, full_name, email, password_hash, role, hospital_id FROM users WHERE (email = ? OR phone_number = ?) AND role IN ('admin', 'super_admin', 'dispatcher') LIMIT 1";
            $stmt = $db->prepare($query);
            $stmt->execute([$identifier, $identifier]);
        } else {
            $query = "SELECT user_id, full_name, email, password_hash, role, hospital_id FROM users WHERE (email = ? OR phone_number = ?) AND role = ? LIMIT 1";
            $stmt = $db->prepare($query);
            $stmt->execute([$identifier, $identifier, $role]);
        }
    } else {
        $query = "SELECT user_id, full_name, email, password_hash, role, hospital_id FROM users WHERE email = ? OR phone_number = ? ORDER BY user_id ASC LIMIT 1";
        $stmt = $db->prepare($query);
        $stmt->execute([$identifier, $identifier]);
    }

    $user = $stmt->fetch();

    // Check credentials
    if (!$user || !password_verify($password, $user['password_hash'])) {
        Response::send(false, "Invalid email or password.", null, 401);
    }

    // Generate Token
    $payload = [
        "user_id" => (int)$user['user_id'],
        "email" => $user['email'],
        "role" => $user['role'],
        "hospital_id" => $user['hospital_id'] !== null ? (int)$user['hospital_id'] : null
    ];
    $token = JWT::encode($payload);

    // Store session
    $sessionExpiry = date('Y-m-d H:i:s', time() + (86400 * 30)); // 30 days
    $sessionQuery = "INSERT INTO login_sessions (user_id, token, expires_at) VALUES (?, ?, ?)";
    $sessionStmt = $db->prepare($sessionQuery);
    $sessionStmt->execute([$user['user_id'], $token, $sessionExpiry]);

    // Get vehicle info if driver
    $vehicle = null;
    if ($user['role'] === 'driver') {
        $ambulanceQuery = "SELECT ambulance_id, vehicle_number, ambulance_type, status FROM ambulances WHERE driver_id = ? LIMIT 1";
        $ambulanceStmt = $db->prepare($ambulanceQuery);
        $ambulanceStmt->execute([$user['user_id']]);
        $ambulance = $ambulanceStmt->fetch();
        
        if ($ambulance) {
            $vehicle = [
                "ambulance_id" => (int)$ambulance['ambulance_id'],
                "vehicle_number" => $ambulance['vehicle_number'],
                "ambulance_type" => $ambulance['ambulance_type'],
                "status" => $ambulance['status']
            ];
        }
    }

    // Fetch Hospital Details (If linked)
    $hospital = null;
    if ($user['hospital_id'] !== null) {
        $hospQuery = "SELECT hospital_id, name, address FROM hospitals WHERE hospital_id = ? LIMIT 1";
        $hospStmt = $db->prepare($hospQuery);
        $hospStmt->execute([$user['hospital_id']]);
        $hospRow = $hospStmt->fetch();
        if ($hospRow) {
            $hospital = [
                "hospital_id" => (int)$hospRow['hospital_id'],
                "name" => $hospRow['name'],
                "address" => $hospRow['address']
            ];
        }
    }

    $data = [
        "token" => $token,
        "user_id" => (int)$user['user_id'],
        "full_name" => $user['full_name'],
        "email" => $user['email'],
        "role" => $user['role'],
        "vehicle" => $vehicle,
        "hospital" => $hospital
    ];
    Response::send(true, "Login successful.", $data, 200);

} catch (Exception $e) {
    Response::send(false, "Authentication failed: " . $e->getMessage(), null, 500);
}
?>
