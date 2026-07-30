<?php
// backend/api/hospital/admissions.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$currentUser = Auth::getAuthenticatedUser();
$role = $currentUser['role'];

// Allow hospital personnel and admins
if (!in_array($role, ['super_admin', 'admin', 'hospital', 'reception', 'doctor'])) {
    Response::send(false, "Forbidden. Clinical staff privileges required.", null, 403);
}

$method = $_SERVER['REQUEST_METHOD'];
$database = new Database();
$db = $database->getConnection();

try {
    // Resolve Hospital ID
    $hospitalId = 0;
    if (in_array($role, ['super_admin', 'admin'])) {
        $hospitalId = isset($_GET['hospital_id']) ? (int)$_GET['hospital_id'] : 0;
    } else {
        $hospitalId = isset($currentUser['hospital_id']) ? (int)$currentUser['hospital_id'] : 0;
    }

    if ($hospitalId <= 0 && $role !== 'doctor') {
        Response::send(false, "No linked hospital facility identified.", null, 400);
    }

    switch ($method) {
        case 'GET':
            // Read admissions list
            $queryParams = [];
            $query = "SELECT pa.*, 
                             doc.full_name AS doctor_name,
                             er.emergency_type, er.pickup_address
                      FROM patient_admissions pa
                      LEFT JOIN users doc ON pa.doctor_id = doc.user_id
                      LEFT JOIN emergency_requests er ON pa.request_id = er.request_id";
            
            $conditions = [];
            if ($role === 'doctor') {
                $conditions[] = "pa.doctor_id = ?";
                $queryParams[] = $currentUser['user_id'];
            } else {
                $conditions[] = "pa.hospital_id = ?";
                $queryParams[] = $hospitalId;
            }

            // Optional status filtering
            if (isset($_GET['status'])) {
                $conditions[] = "pa.status = ?";
                $queryParams[] = trim($_GET['status']);
            }

            if (!empty($conditions)) {
                $query .= " WHERE " . implode(" AND ", $conditions);
            }
            $query .= " ORDER BY pa.admitted_at DESC";

            $stmt = $db->prepare($query);
            $stmt->execute($queryParams);
            $admissions = $stmt->fetchAll(PDO::FETCH_ASSOC);

            Response::send(true, "Patient charts list retrieved successfully.", $admissions, 200);
            break;

        case 'POST':
            // New Patient Intake (Admission)
            $input = json_decode(file_get_contents("php://input"), true);
            $requestId = isset($input['request_id']) && (int)$input['request_id'] > 0 ? (int)$input['request_id'] : null;
            $patientName = isset($input['patient_name']) ? trim($input['patient_name']) : '';
            $patientPhone = isset($input['patient_phone']) ? trim($input['patient_phone']) : '';
            $bedType = isset($input['bed_type']) ? trim($input['bed_type']) : 'general';
            $doctorId = isset($input['doctor_id']) && (int)$input['doctor_id'] > 0 ? (int)$input['doctor_id'] : null;

            if (empty($patientName) || !in_array($bedType, ['ICU', 'NICU', 'general'])) {
                Response::send(false, "Patient name and valid bed type are required.", null, 400);
            }

            $db->beginTransaction();

            // 1. Verify bed availability
            $bedCol = "available_" . ($bedType === 'general' ? 'general' : strtolower($bedType)) . "_beds";
            $chkQuery = "SELECT $bedCol FROM hospitals WHERE hospital_id = ? LIMIT 1 FOR UPDATE";
            $chkStmt = $db->prepare($chkQuery);
            $chkStmt->execute([$hospitalId]);
            $hospRow = $chkStmt->fetch();

            if (!$hospRow || (int)$hospRow[$bedCol] <= 0) {
                $db->rollBack();
                Response::send(false, "Insufficient $bedType bed capacity at this facility.", null, 409);
            }

            // 2. Decrement bed capacity
            $decQuery = "UPDATE hospitals SET $bedCol = $bedCol - 1 WHERE hospital_id = ?";
            $decStmt = $db->prepare($decQuery);
            $decStmt->execute([$hospitalId]);

            // 3. Create admission entry
            $insertQuery = "INSERT INTO patient_admissions (request_id, hospital_id, patient_name, patient_phone, bed_type, doctor_id, status)
                            VALUES (?, ?, ?, ?, ?, ?, 'admitted')";
            $insertStmt = $db->prepare($insertQuery);
            $insertStmt->execute([$requestId, $hospitalId, $patientName, $patientPhone, $bedType, $doctorId]);
            $admissionId = $db->lastInsertId();

            $db->commit();

            Response::send(true, "Patient admitted and bed capacity reserved.", ["admission_id" => (int)$admissionId], 201);
            break;

        case 'PUT':
            // Update patient charts / prescriptions / discharges
            $input = json_decode(file_get_contents("php://input"), true);
            $admissionId = isset($input['admission_id']) ? (int)$input['admission_id'] : 0;
            $notes = isset($input['treatment_notes']) ? trim($input['treatment_notes']) : null;
            $prescriptions = isset($input['prescriptions']) ? trim($input['prescriptions']) : null;
            $status = isset($input['status']) ? trim($input['status']) : null;
            $doctorId = isset($input['doctor_id']) && (int)$input['doctor_id'] > 0 ? (int)$input['doctor_id'] : null;

            if ($admissionId <= 0) {
                Response::send(false, "Valid admission ID is required.", null, 400);
            }

            $db->beginTransaction();

            // Fetch current admission state
            $stateQuery = "SELECT hospital_id, bed_type, status FROM patient_admissions WHERE admission_id = ? LIMIT 1 FOR UPDATE";
            $stateStmt = $db->prepare($stateQuery);
            $stateStmt->execute([$admissionId]);
            $current = $stateStmt->fetch();

            if (!$current) {
                $db->rollBack();
                Response::send(false, "Intake chart not found.", null, 404);
            }

            // Build dynamic update properties
            $updateFields = [];
            $updateParams = [];

            if ($notes !== null) {
                $updateFields[] = "treatment_notes = ?";
                $updateParams[] = $notes;
            }
            if ($prescriptions !== null) {
                $updateFields[] = "prescriptions = ?";
                $updateParams[] = $prescriptions;
            }
            if ($doctorId !== null) {
                $updateFields[] = "doctor_id = ?";
                $updateParams[] = $doctorId;
            }

            // Handle Patient Discharge
            if ($status === 'discharged' && $current['status'] === 'admitted') {
                $updateFields[] = "status = ?";
                $updateParams[] = 'discharged';
                $updateFields[] = "discharged_at = CURRENT_TIMESTAMP";

                // Increment bed capacity
                $bedType = $current['bed_type'];
                $bedCol = "available_" . ($bedType === 'general' ? 'general' : strtolower($bedType)) . "_beds";
                $incQuery = "UPDATE hospitals SET $bedCol = $bedCol + 1 WHERE hospital_id = ?";
                $incStmt = $db->prepare($incQuery);
                $incStmt->execute([$current['hospital_id']]);
            }

            if (!empty($updateFields)) {
                $updateParams[] = $admissionId;
                $upQuery = "UPDATE patient_admissions SET " . implode(", ", $updateFields) . " WHERE admission_id = ?";
                $upStmt = $db->prepare($upQuery);
                $upStmt->execute($updateParams);
            }

            $db->commit();

            Response::send(true, "Patient clinical chart updated successfully.", null, 200);
            break;

        default:
            Response::send(false, "Method not allowed. Use GET, POST, or PUT.", null, 405);
    }
} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    Response::send(false, "Operation failed: " . $e->getMessage(), null, 500);
}
?>
