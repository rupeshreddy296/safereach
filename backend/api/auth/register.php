<?php
// backend/api/auth/register.php

require_once '../../config/db.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);

// Extract parameters
$fullName = isset($input['full_name']) ? trim($input['full_name']) : '';
$phoneNumber = isset($input['phone_number']) && !empty(trim($input['phone_number'])) ? trim($input['phone_number']) : null;
$email = isset($input['email']) ? trim($input['email']) : '';
$password = isset($input['password']) ? trim($input['password']) : '';
$role = isset($input['role']) ? strtolower(trim($input['role'])) : 'user'; // 'user', 'driver', 'admin'
$vehicleNumber = isset($input['vehicle_number']) ? trim($input['vehicle_number']) : null;
$ambulanceType = isset($input['ambulance_type']) ? trim($input['ambulance_type']) : 'BLS'; // 'BLS', 'ALS', 'CCU'
$driverRole    = isset($input['driver_role'])    ? trim($input['driver_role'])    : 'Ambulance Driver';
$licenseNumber = isset($input['license_number']) ? trim($input['license_number']) : null;
$licenseExpiry = isset($input['license_expiry']) ? trim($input['license_expiry']) : null;
$experienceYears = isset($input['experience_years']) ? (int)$input['experience_years'] : 0;
// Validate required fields
if (empty($fullName) || empty($email) || empty($password)) {
    Response::send(false, "Missing required fields. Please fill in full name, email, and password.", null, 400);
}

// Normalize role validation
if (!in_array($role, ['user', 'driver', 'admin'])) {
    Response::send(false, "Invalid role. Role must be user, driver, or admin.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

// Auto-migrate unique constraints to allow same email/phone for different roles
try {
    $db->exec("ALTER TABLE users DROP INDEX uq_user_email");
    $db->exec("ALTER TABLE users DROP INDEX uq_user_phone");
    $db->exec("ALTER TABLE users ADD CONSTRAINT uq_user_email_role UNIQUE (email, role)");
    $db->exec("ALTER TABLE users ADD CONSTRAINT uq_user_phone_role UNIQUE (phone_number, role)");
} catch (Exception $e) {
    // Ignore if they are already dropped/added
}

try {
    // Allow same email across different roles — only block exact email+role duplicate
    $checkQuery = "SELECT user_id FROM users WHERE email = ? AND role = ? LIMIT 1";
    $checkStmt = $db->prepare($checkQuery);
    $checkStmt->execute([$email, $role]);
    if ($checkStmt->rowCount() > 0) {
        Response::send(false, "This email is already registered as a " . ucfirst($role) . ". Please use a different email or sign in.", null, 409);
    }

    // If phone provided, only block if same phone+role already exists
    if ($phoneNumber !== null) {
        $checkPhoneQuery = "SELECT user_id FROM users WHERE phone_number = ? AND role = ? LIMIT 1";
        $checkPhoneStmt = $db->prepare($checkPhoneQuery);
        $checkPhoneStmt->execute([$phoneNumber, $role]);
        if ($checkPhoneStmt->rowCount() > 0) {
            Response::send(false, "This phone number is already registered as a " . ucfirst($role) . ".", null, 409);
        }
    }

    $db->beginTransaction();

    // Hash password
    $passwordHash = password_hash($password, PASSWORD_BCRYPT);

    // Insert user into DB
    $insertQuery = "INSERT INTO users (full_name, phone_number, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
    $insertStmt = $db->prepare($insertQuery);
    $insertStmt->execute([$fullName, $phoneNumber, $email, $passwordHash, $role]);
    
    $userId = (int)$db->lastInsertId();

    // If driver, we must create a record in the ambulances table
    if ($role === 'driver') {
        if (empty($vehicleNumber)) {
            $db->rollBack();
            Response::send(false, "Vehicle number is required for driver registration.", null, 400);
        }
        
        // Check if vehicle number is already registered
        $checkAmbulance = "SELECT ambulance_id FROM ambulances WHERE vehicle_number = ? LIMIT 1";
        $checkAmbulanceStmt = $db->prepare($checkAmbulance);
        $checkAmbulanceStmt->execute([$vehicleNumber]);
        if ($checkAmbulanceStmt->rowCount() > 0) {
            $db->rollBack();
            Response::send(false, "Vehicle number is already registered to another driver.", null, 409);
        }

        // Insert into ambulances
        $ambulanceQuery = "INSERT INTO ambulances (driver_id, vehicle_number, ambulance_type, driver_role, license_number, license_expiry, experience_years, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'offline')";
        $ambulanceStmt = $db->prepare($ambulanceQuery);
        $ambulanceStmt->execute([$userId, $vehicleNumber, $ambulanceType, $driverRole, $licenseNumber, $licenseExpiry, $experienceYears]);
    }

    $db->commit();

    $data = [
        "user_id" => $userId,
        "full_name" => $fullName,
        "phone_number" => $phoneNumber,
        "email" => $email,
        "role" => $role
    ];

    Response::send(true, "Registration successful.", $data, 201);

} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    Response::send(false, "Registration failed: " . $e->getMessage(), null, 500);
}
?>
