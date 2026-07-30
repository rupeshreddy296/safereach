<?php
// backend/api/debug/seed.php

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST");

$message = "";
$status = "info";
$seeded = false;

if (isset($_POST['run_seed'])) {
    try {
        $host = "localhost";
        $username = "root";
        $password = "";
        
        $pdo = new PDO("mysql:host=$host;charset=utf8mb4", $username, $password);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        
        // Re-create database
        $pdo->exec("DROP DATABASE IF EXISTS safereach");
        $pdo->exec("CREATE DATABASE safereach CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        $pdo->exec("USE safereach");
        
        // Import schema
        $schemaPath = '../../../database/schema.sql';
        if (file_exists($schemaPath)) {
            $schemaSql = file_get_contents($schemaPath);
            // Clean out CREATE/USE database lines
            $schemaSql = preg_replace('/CREATE DATABASE IF NOT EXISTS.*?;/i', '', $schemaSql);
            $schemaSql = preg_replace('/USE safereach;/i', '', $schemaSql);
            $pdo->exec($schemaSql);
        } else {
            throw new Exception("Could not locate schema.sql in database folder.");
        }
        
        // --- SEED DATA ---
        
        // 1. Seed System Settings
        $settings = [
            ['sms_gateway', 'twilio'],
            ['rate_limit_cooldown', '30'],
            ['session_expiry_days', '30'],
            ['google_maps_api_key', '']
        ];
        $settStmt = $pdo->prepare("INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?)");
        foreach ($settings as $setting) {
            $settStmt->execute($setting);
        }

        // 2. Seed Hospitals
        $hospitals = [
            ['Apollo Hospitals, Greams Road', '21, Greams Lane, Off Greams Road, Chennai, Tamil Nadu 600006', '+914428290200', 13.0602, 80.2511, 15, 8, 40],
            ['Fortis Malar Hospital, Adyar', '52, 1st Main Rd, Gandhi Nagar, Adyar, Chennai, Tamil Nadu 600020', '+914442892222', 13.0076, 80.2568, 10, 5, 25],
            ['Saveetha Medical College Hospital', 'Saveetha Nagar, Thandalam, Chennai, Tamil Nadu 602105', '+914466726672', 13.0275, 80.0178, 20, 10, 50]
        ];
        $hospStmt = $pdo->prepare("INSERT INTO hospitals (name, address, phone_number, latitude, longitude, available_icu_beds, available_nicu_beds, available_general_beds) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        foreach ($hospitals as $h) {
            $hospStmt->execute($h);
        }

        // Get hospital IDs
        $apolloId = 1;
        $fortisId = 2;
        $saveethaId = 3;

        // 3. Seed Users
        $passwordHash = password_hash('password123', PASSWORD_BCRYPT);
        $users = [
            ['Super Admin', '+919999999991', 'superadmin@safereach.com', $passwordHash, 'super_admin', null],
            ['Admin Director', '+919999999992', 'admin@safereach.com', $passwordHash, 'admin', null],
            ['Ambulance Dispatcher', '+919999999993', 'dispatcher@safereach.com', $passwordHash, 'dispatcher', null],
            ['Ambulance Driver TN-07', '+919999999994', 'driver@safereach.com', $passwordHash, 'driver', null],
            ['Apollo Admin', '+919999999995', 'hospital@safereach.com', $passwordHash, 'hospital', $apolloId],
            ['Dr. Ramesh Kumar', '+919999999996', 'doctor@safereach.com', $passwordHash, 'doctor', $apolloId],
            ['Apollo Receptionist', '+919999999997', 'reception@safereach.com', $passwordHash, 'reception', $apolloId],
            ['Data Analyst', '+919999999998', 'analytics@safereach.com', $passwordHash, 'analytics', null],
            ['Rupesh Reddy', '+919999999999', 'citizen@safereach.com', $passwordHash, 'user', null],
        ];

        $userStmt = $pdo->prepare("INSERT INTO users (full_name, phone_number, email, password_hash, role, hospital_id) VALUES (?, ?, ?, ?, ?, ?)");
        foreach ($users as $u) {
            $userStmt->execute($u);
        }

        // Resolve driver and citizen IDs
        $driverId = 4;
        $citizenId = 9;

        // 4. Seed Ambulance for Driver
        $ambQuery = "INSERT INTO ambulances (driver_id, vehicle_number, ambulance_type, status, current_latitude, current_longitude) VALUES (?, 'TN-07-SR-1234', 'ALS', 'online', 13.0232, 80.0175)";
        $pdo->prepare($ambQuery)->execute([$driverId]);

        // 5. Seed Emergency Request
        $reqQuery = "INSERT INTO emergency_requests (user_id, patient_name, patient_phone, pickup_address, pickup_latitude, pickup_longitude, requested_ambulance_type, emergency_type, status) VALUES (?, 'Rupesh Reddy', '+919999999999', 'Saveetha Engineering College campus, Thandalam', 13.0232, 80.0175, 'ALS', 'Cardiac arrest', 'pending')";
        $pdo->prepare($reqQuery)->execute([$citizenId]);

        // 6. Seed Audit Logs
        $auditQuery = "INSERT INTO audit_logs (user_id, action, details, ip_address) VALUES (1, 'system_seed', 'System database successfully seeded with test roles and defaults.', '127.0.0.1')";
        $pdo->exec($auditQuery);

        $message = "Database reset and seeded successfully! 9 role-specific test accounts, hospitals, settings, and active ambulance requests are now active.";
        $status = "success";
        $seeded = true;
    } catch (Exception $e) {
        $message = "Database initialization failed: " . $e->getMessage();
        $status = "error";
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SafeReach | Clean Database Reset & Seeder</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-primary: #0a101d;
            --bg-secondary: #121929;
            --text-primary: #f1f5f9;
            --text-secondary: #94a3b8;
            --accent-green: #10b981;
            --accent-red: #ef4444;
            --accent-blue: #3b82f6;
            --border-color: rgba(255,255,255,0.08);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            background-color: var(--bg-primary);
            color: var(--text-primary);
            font-family: 'Inter', sans-serif;
            padding: 40px 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }
        .container {
            width: 100%;
            max-width: 800px;
            background: var(--bg-secondary);
            border: 1px solid var(--border-color);
            padding: 40px;
            border-radius: 20px;
            box-shadow: 0 10px 45px rgba(0,0,0,0.5);
        }
        h1 {
            font-family: 'Outfit', sans-serif;
            font-size: 32px;
            margin-bottom: 8px;
            text-align: center;
            letter-spacing: -0.02em;
        }
        .subtitle {
            color: var(--text-secondary);
            text-align: center;
            margin-bottom: 30px;
            font-size: 15px;
        }
        .alert {
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 25px;
            font-size: 14px;
            font-weight: 500;
            word-break: break-word;
        }
        .alert-success { background: rgba(16,185,129,0.15); border: 1px solid var(--accent-green); color: var(--accent-green); }
        .alert-error { background: rgba(239,68,68,0.15); border: 1px solid var(--accent-red); color: var(--accent-red); }
        .alert-info { background: rgba(59,130,246,0.15); border: 1px solid var(--accent-blue); color: var(--accent-blue); }
        
        .btn {
            display: block;
            width: 100%;
            padding: 15px;
            background: var(--accent-red);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            text-align: center;
            text-decoration: none;
            font-family: 'Outfit', sans-serif;
        }
        .btn:hover { background: #dc2626; }
        .btn-blue { background: var(--accent-blue); margin-top: 15px; }
        .btn-blue:hover { background: #2563eb; }

        .accounts-table {
            width: 100%;
            border-collapse: collapse;
            margin: 25px 0;
            font-size: 13px;
        }
        .accounts-table th, .accounts-table td {
            padding: 10px 12px;
            text-align: left;
            border-bottom: 1px solid var(--border-color);
        }
        .accounts-table th {
            color: var(--text-secondary);
            font-weight: 600;
            font-family: 'Outfit', sans-serif;
        }
        .accounts-table td {
            color: var(--text-primary);
        }
        .badge {
            display: inline-block;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: bold;
            text-transform: uppercase;
        }
        .badge-red { background: rgba(239,68,68,0.15); color: var(--accent-red); }
        .badge-blue { background: rgba(59,130,246,0.15); color: var(--accent-blue); }
        .badge-green { background: rgba(16,185,129,0.15); color: var(--accent-green); }
    </style>
</head>
<body>
    <div class="container">
        <h1>SafeReach Clean Reset & Seeder</h1>
        <p class="subtitle">Drops the current database, constructs tables from schema.sql, and seeds test accounts.</p>

        <?php if (!empty($message)): ?>
            <div class="alert alert-<?php echo $status; ?>">
                <?php echo $message; ?>
            </div>
        <?php endif; ?>

        <form method="POST">
            <button type="submit" name="run_seed" class="btn">⚠️ Reset & Seed System Database</button>
        </form>

        <h3 style="margin-top:30px; font-family:'Outfit';">Seed Credentials (Password: <code>password123</code>)</h3>
        <table class="accounts-table">
            <thead>
                <tr>
                    <th>Role</th>
                    <th>Phone Number</th>
                    <th>Email Address</th>
                    <th>Context</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><span class="badge badge-red">super_admin</span></td>
                    <td><code>+919999999991</code></td>
                    <td>superadmin@safereach.com</td>
                    <td>Full access</td>
                </tr>
                <tr>
                    <td><span class="badge badge-red">admin</span></td>
                    <td><code>+919999999992</code></td>
                    <td>admin@safereach.com</td>
                    <td>Incident board</td>
                </tr>
                <tr>
                    <td><span class="badge badge-red">dispatcher</span></td>
                    <td><code>+919999999993</code></td>
                    <td>dispatcher@safereach.com</td>
                    <td>Queue & map</td>
                </tr>
                <tr>
                    <td><span class="badge badge-green">driver</span></td>
                    <td><code>+919999999994</code></td>
                    <td>driver@safereach.com</td>
                    <td>TN-07-SR-1234 (ALS)</td>
                </tr>
                <tr>
                    <td><span class="badge badge-blue">hospital</span></td>
                    <td><code>+919999999995</code></td>
                    <td>hospital@safereach.com</td>
                    <td>Apollo Admin</td>
                </tr>
                <tr>
                    <td><span class="badge badge-blue">doctor</span></td>
                    <td><code>+919999999996</code></td>
                    <td>doctor@safereach.com</td>
                    <td>Dr. Ramesh Kumar (Apollo)</td>
                </tr>
                <tr>
                    <td><span class="badge badge-blue">reception</span></td>
                    <td><code>+919999999997</code></td>
                    <td>reception@safereach.com</td>
                    <td>Apollo Front Desk</td>
                </tr>
                <tr>
                    <td><span class="badge badge-red">analytics</span></td>
                    <td><code>+919999999998</code></td>
                    <td>analytics@safereach.com</td>
                    <td>Reports</td>
                </tr>
                <tr>
                    <td><span class="badge badge-green">citizen</span></td>
                    <td><code>+919999999999</code></td>
                    <td>citizen@safereach.com</td>
                    <td>Rupesh Reddy</td>
                </tr>
            </tbody>
        </table>

        <?php if ($seeded): ?>
            <a href="../../../frontend/" class="btn btn-blue">⚡ Open SafeReach HTML/JS App</a>
        <?php endif; ?>
    </div>
</body>
</html>
