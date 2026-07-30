<?php
// backend/api/reports/export.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

// Authenticate user
$currentUser = Auth::getAuthenticatedUser();
if (!in_array($currentUser['role'], ['super_admin', 'admin', 'analytics'])) {
    Response::send(false, "Forbidden. Analytics clearance required.", null, 403);
}

$reportType = isset($_GET['type']) ? trim($_GET['type']) : '';
if (empty($reportType)) {
    Response::send(false, "Report 'type' query parameter is required (e.g., users, drivers, hospitals, requests, ambulances, audits).", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    $data = [];
    $headers = [];
    $filename = "safereach_report_" . $reportType . "_" . date('Ymd_His') . ".csv";

    switch ($reportType) {
        case 'users':
            $headers = ['User ID', 'Full Name', 'Phone Number', 'Email', 'Role', 'Created At'];
            $query = "SELECT user_id, full_name, phone_number, email, role, created_at FROM users ORDER BY created_at DESC";
            $stmt = $db->query($query);
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            break;

        case 'drivers':
            $headers = ['User ID', 'Driver Name', 'Phone Number', 'Email', 'Vehicle Number', 'Ambulance Type', 'Status'];
            $query = "SELECT u.user_id, u.full_name, u.phone_number, u.email, a.vehicle_number, a.ambulance_type, a.status
                      FROM users u
                      INNER JOIN ambulances a ON u.user_id = a.driver_id
                      WHERE u.role = 'driver'
                      ORDER BY u.created_at DESC";
            $stmt = $db->query($query);
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            break;

        case 'hospitals':
            $headers = ['Hospital ID', 'Facility Name', 'Address', 'Phone', 'ICU Beds', 'NICU Beds', 'General Beds', 'Status'];
            $query = "SELECT hospital_id, name, address, phone_number, available_icu_beds, available_nicu_beds, available_general_beds, status 
                      FROM hospitals ORDER BY name ASC";
            $stmt = $db->query($query);
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            break;

        case 'requests':
            $headers = ['Request ID', 'Patient Name', 'Phone', 'Pickup Address', 'Ambulance Type', 'Emergency Type', 'Status', 'Created At', 'Completed At'];
            $query = "SELECT request_id, patient_name, patient_phone, pickup_address, requested_ambulance_type, emergency_type, status, created_at, completed_at 
                      FROM emergency_requests ORDER BY created_at DESC";
            $stmt = $db->query($query);
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            break;

        case 'ambulances':
            $headers = ['Ambulance ID', 'Vehicle Number', 'Ambulance Type', 'Driver ID', 'Status', 'Last Updated'];
            $query = "SELECT ambulance_id, vehicle_number, ambulance_type, driver_id, status, updated_at FROM ambulances ORDER BY vehicle_number ASC";
            $stmt = $db->query($query);
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            break;

        case 'audits':
            $headers = ['Log ID', 'User ID', 'User Name', 'Action', 'Details', 'IP Address', 'Timestamp'];
            $query = "SELECT a.log_id, a.user_id, u.full_name, a.action, a.details, a.ip_address, a.created_at
                      FROM audit_logs a
                      LEFT JOIN users u ON a.user_id = u.user_id
                      ORDER BY a.created_at DESC";
            $stmt = $db->query($query);
            $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
            break;

        default:
            Response::send(false, "Unsupported report type.", null, 400);
            exit();
    }

    // Set download headers
    header('Content-Type: text/csv; charset=utf-8');
    header('Content-Disposition: attachment; filename="' . $filename . '"');
    header('Pragma: no-cache');
    header('Expires: 0');

    // Write CSV structure to output stream
    $output = fopen('php://output', 'w');
    
    // Add UTF-8 BOM for Excel compatibility
    fprintf($output, chr(0xEF).chr(0xBB).chr(0xBF));
    
    // Write CSV Headers
    fputcsv($output, $headers);

    // Write CSV Data rows
    foreach ($data as $row) {
        fputcsv($output, $row);
    }
    
    fclose($output);
    exit();

} catch (Exception $e) {
    Response::send(false, "Export compilation failed: " . $e->getMessage(), null, 500);
}
?>
