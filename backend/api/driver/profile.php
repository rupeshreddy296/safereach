<?php
// backend/api/driver/profile.php
// GET  — fetch full driver profile with stats
// PUT  — update driver profile fields

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

$auth = JWT::authenticate();
if (!$auth || $auth['role'] !== 'driver') {
    Response::send(false, "Unauthorized. Driver access only.", null, 401);
}
$driverId = (int)$auth['user_id'];

$database = new Database();
$db = $database->getConnection();

/* ──────────────────────────────────────────
   GET — Full profile snapshot
────────────────────────────────────────── */
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    try {
        // 1. User + ambulance details
        $query = "
            SELECT
                u.user_id,
                u.full_name,
                u.email,
                u.phone_number,
                u.profile_photo,
                u.created_at AS member_since,
                a.ambulance_id,
                a.vehicle_number,
                a.ambulance_type,
                a.license_number,
                a.license_expiry,
                a.experience_years,
                a.driver_role,
                a.status AS duty_status,
                a.current_latitude,
                a.current_longitude
            FROM users u
            LEFT JOIN ambulances a ON a.driver_id = u.user_id
            WHERE u.user_id = ?
            LIMIT 1
        ";
        $stmt = $db->prepare($query);
        $stmt->execute([$driverId]);
        $profile = $stmt->fetch();

        if (!$profile) {
            Response::send(false, "Driver profile not found.", null, 404);
        }

        // 2. Overall rating (avg across all completed trips)
        $ratingQuery = "
            SELECT
                COUNT(rf.feedback_id)   AS total_trips,
                COALESCE(AVG(rf.rating), 0) AS avg_rating
            FROM emergency_requests er
            JOIN ambulances a ON a.ambulance_id = er.assigned_ambulance_id
            LEFT JOIN request_feedback rf ON rf.request_id = er.request_id
            WHERE a.driver_id = ? AND er.status = 'completed'
        ";
        $rStmt = $db->prepare($ratingQuery);
        $rStmt->execute([$driverId]);
        $stats = $rStmt->fetch();

        // 3. Trip history (last 20)
        $tripQuery = "
            SELECT
                er.request_id,
                er.patient_name,
                er.emergency_type,
                er.pickup_address,
                er.status,
                er.created_at,
                er.completed_at,
                h.name  AS hospital_name,
                rf.rating,
                rf.comments
            FROM emergency_requests er
            JOIN ambulances a ON a.ambulance_id = er.assigned_ambulance_id
            LEFT JOIN hospitals h ON h.hospital_id = er.destination_hospital_id
            LEFT JOIN request_feedback rf ON rf.request_id = er.request_id
            WHERE a.driver_id = ?
            ORDER BY er.created_at DESC
            LIMIT 20
        ";
        $tStmt = $db->prepare($tripQuery);
        $tStmt->execute([$driverId]);
        $trips = $tStmt->fetchAll();

        // 4. Active / pending ambulance request right now
        $activeQuery = "
            SELECT
                er.request_id,
                er.patient_name,
                er.patient_phone,
                er.emergency_type,
                er.pickup_address,
                er.pickup_latitude,
                er.pickup_longitude,
                er.requested_ambulance_type,
                er.status,
                er.created_at,
                h.name      AS hospital_name,
                h.address   AS hospital_address
            FROM emergency_requests er
            JOIN ambulances a ON a.ambulance_id = er.assigned_ambulance_id
            LEFT JOIN hospitals h ON h.hospital_id = er.destination_hospital_id
            WHERE a.driver_id = ?
              AND er.status IN ('pending','accepted','arrived','boarded')
            ORDER BY er.created_at DESC
            LIMIT 1
        ";
        $aStmt = $db->prepare($activeQuery);
        $aStmt->execute([$driverId]);
        $activeRequest = $aStmt->fetch() ?: null;

        $data = [
            "user_id"          => (int)$profile['user_id'],
            "full_name"        => $profile['full_name'],
            "email"            => $profile['email'],
            "phone_number"     => $profile['phone_number'],
            "profile_photo"    => $profile['profile_photo'],
            "member_since"     => $profile['member_since'],
            "ambulance_id"     => $profile['ambulance_id'] ? (int)$profile['ambulance_id'] : null,
            "vehicle_number"   => $profile['vehicle_number'],
            "ambulance_type"   => $profile['ambulance_type'],
            "license_number"   => $profile['license_number'],
            "license_expiry"   => $profile['license_expiry'],
            "experience_years" => (int)$profile['experience_years'],
            "driver_role"      => $profile['driver_role'],
            "duty_status"      => $profile['duty_status'],
            "total_trips"      => (int)$stats['total_trips'],
            "avg_rating"       => round((float)$stats['avg_rating'], 1),
            "trip_history"     => $trips,
            "active_request"   => $activeRequest,
        ];

        Response::send(true, "Driver profile fetched.", $data, 200);
    } catch (Exception $e) {
        Response::send(false, "Failed: " . $e->getMessage(), null, 500);
    }
}

/* ──────────────────────────────────────────
   PUT — Update profile
────────────────────────────────────────── */
elseif ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    $input = json_decode(file_get_contents("php://input"), true);

    try {
        $db->beginTransaction();

        // Update users table
        $fullName    = isset($input['full_name'])     ? trim($input['full_name'])     : null;
        $phoneNumber = isset($input['phone_number'])  ? trim($input['phone_number'])  : null;
        $photoUrl    = isset($input['profile_photo']) ? trim($input['profile_photo']) : null;

        $userFields = [];
        $userParams = [];
        if ($fullName)    { $userFields[] = "full_name = ?";    $userParams[] = $fullName; }
        if ($phoneNumber) { $userFields[] = "phone_number = ?"; $userParams[] = $phoneNumber; }
        if ($photoUrl !== null) { $userFields[] = "profile_photo = ?"; $userParams[] = $photoUrl; }

        if (!empty($userFields)) {
            $userParams[] = $driverId;
            $uStmt = $db->prepare("UPDATE users SET " . implode(', ', $userFields) . " WHERE user_id = ?");
            $uStmt->execute($userParams);
        }

        // Update ambulances table
        $licenseNumber  = isset($input['license_number'])  ? trim($input['license_number'])  : null;
        $licenseExpiry  = isset($input['license_expiry'])  ? trim($input['license_expiry'])  : null;
        $experienceYrs  = isset($input['experience_years'])? (int)$input['experience_years']  : null;
        $driverRole     = isset($input['driver_role'])     ? trim($input['driver_role'])      : null;
        $ambulanceType  = isset($input['ambulance_type'])  ? trim($input['ambulance_type'])   : null;
        $vehicleNumber  = isset($input['vehicle_number'])  ? trim($input['vehicle_number'])   : null;

        $ambFields = [];
        $ambParams = [];
        if ($licenseNumber !== null) { $ambFields[] = "license_number = ?";  $ambParams[] = $licenseNumber; }
        if ($licenseExpiry !== null) { $ambFields[] = "license_expiry = ?";  $ambParams[] = $licenseExpiry; }
        if ($experienceYrs !== null) { $ambFields[] = "experience_years = ?";$ambParams[] = $experienceYrs; }
        if ($driverRole    !== null) { $ambFields[] = "driver_role = ?";     $ambParams[] = $driverRole; }
        if ($ambulanceType !== null) { $ambFields[] = "ambulance_type = ?";  $ambParams[] = $ambulanceType; }
        if ($vehicleNumber !== null) { $ambFields[] = "vehicle_number = ?";  $ambParams[] = $vehicleNumber; }

        if (!empty($ambFields)) {
            $ambParams[] = $driverId;
            $aStmt = $db->prepare("UPDATE ambulances SET " . implode(', ', $ambFields) . " WHERE driver_id = ?");
            $aStmt->execute($ambParams);
        }

        $db->commit();
        Response::send(true, "Profile updated successfully.", null, 200);
    } catch (Exception $e) {
        $db->rollBack();
        Response::send(false, "Update failed: " . $e->getMessage(), null, 500);
    }
}

else {
    Response::send(false, "Method not allowed.", null, 405);
}
?>
