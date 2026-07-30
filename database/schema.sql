-- Create database if not exists
CREATE DATABASE IF NOT EXISTS safereach CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE safereach;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('user', 'driver', 'admin') NOT NULL DEFAULT 'user',
    emergency_contact_name VARCHAR(100) NULL,
    emergency_contact_phone VARCHAR(20) NULL,
    blood_group VARCHAR(5) NULL,
    allergies TEXT NULL,
    chronic_conditions TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_phone UNIQUE (phone_number),
    CONSTRAINT uq_user_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for authentication and search by phone
CREATE INDEX idx_users_phone ON users(phone_number);

-- 2. SAVED_ADDRESSES TABLE
CREATE TABLE IF NOT EXISTS saved_addresses (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(50) NOT NULL,
    subtitle VARCHAR(100) NULL,
    address TEXT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_addresses_user ON saved_addresses(user_id);

-- 3. AMBULANCES TABLE
CREATE TABLE IF NOT EXISTS ambulances (
    ambulance_id INT AUTO_INCREMENT PRIMARY KEY,
    driver_id INT NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    ambulance_type ENUM('BLS', 'ALS', 'CCU') NOT NULL DEFAULT 'BLS',
    status ENUM('online', 'busy', 'offline') NOT NULL DEFAULT 'offline',
    current_latitude DECIMAL(10, 8) NULL,
    current_longitude DECIMAL(11, 8) NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_ambulance_driver UNIQUE (driver_id),
    CONSTRAINT uq_ambulance_vehicle UNIQUE (vehicle_number),
    CONSTRAINT fk_ambulances_driver FOREIGN KEY (driver_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for spatial matching of online units
CREATE INDEX idx_ambulances_status_coords ON ambulances(status, current_latitude, current_longitude);

-- 4. HOSPITALS TABLE
CREATE TABLE IF NOT EXISTS hospitals (
    hospital_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    phone_number VARCHAR(20) NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    available_icu_beds INT NOT NULL DEFAULT 0,
    available_nicu_beds INT NOT NULL DEFAULT 0,
    status ENUM('active', 'inactive') NOT NULL DEFAULT 'active',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 5. EMERGENCY_REQUESTS TABLE
CREATE TABLE IF NOT EXISTS emergency_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    patient_phone VARCHAR(20) NOT NULL,
    pickup_address TEXT NOT NULL,
    pickup_latitude DECIMAL(10, 8) NOT NULL,
    pickup_longitude DECIMAL(11, 8) NOT NULL,
    requested_ambulance_type ENUM('BLS', 'ALS', 'CCU') NOT NULL DEFAULT 'BLS',
    emergency_type VARCHAR(50) NOT NULL,
    description TEXT NULL,
    assigned_ambulance_id INT NULL,
    destination_hospital_id INT NULL,
    status ENUM('pending', 'accepted', 'arrived', 'boarded', 'completed', 'cancelled') NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP NULL,
    arrived_at TIMESTAMP NULL,
    boarded_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    CONSTRAINT fk_requests_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_requests_ambulance FOREIGN KEY (assigned_ambulance_id) REFERENCES ambulances(ambulance_id) ON DELETE SET NULL,
    CONSTRAINT fk_requests_hospital FOREIGN KEY (destination_hospital_id) REFERENCES hospitals(hospital_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_requests_status ON emergency_requests(status);
CREATE INDEX idx_requests_user ON emergency_requests(user_id);

-- 6. PATIENT_VITALS TABLE
CREATE TABLE IF NOT EXISTS patient_vitals (
    vitals_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    heart_rate INT NULL,
    blood_pressure VARCHAR(20) NULL, -- Format: SYS/DIA (e.g. 120/80)
    oxygen_saturation INT NULL,
    temperature DECIMAL(4, 1) NULL,
    patient_condition ENUM('stable', 'critical', 'unconscious', 'improving') NOT NULL DEFAULT 'stable',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_vitals_request UNIQUE (request_id),
    CONSTRAINT fk_vitals_request FOREIGN KEY (request_id) REFERENCES emergency_requests(request_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. REQUEST_FEEDBACK TABLE
CREATE TABLE IF NOT EXISTS request_feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_feedback_request UNIQUE (request_id),
    CONSTRAINT fk_feedback_request FOREIGN KEY (request_id) REFERENCES emergency_requests(request_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    type ENUM('info', 'alert', 'success') NOT NULL DEFAULT 'info',
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);

-- 9. PHONE_OTPS TABLE
CREATE TABLE IF NOT EXISTS phone_otps (
    phone_number VARCHAR(20) PRIMARY KEY,
    otp_code VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. OTP_VERIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS otp_verifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    otp_code VARCHAR(6) NOT NULL,
    resend_count INT DEFAULT 0,
    last_sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    is_used TINYINT(1) DEFAULT 0,
    INDEX idx_phone_otp (phone_number, otp_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. LOGIN_SESSIONS TABLE
CREATE TABLE IF NOT EXISTS login_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NULL,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. LOCATION_HISTORY TABLE
CREATE TABLE IF NOT EXISTS location_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. GPS_TRACKING TABLE
CREATE TABLE IF NOT EXISTS gps_tracking (
    tracking_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tracking_request FOREIGN KEY (request_id) REFERENCES emergency_requests(request_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. SMS_LOGS TABLE
CREATE TABLE IF NOT EXISTS sms_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    gateway VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. ALTERS AND NEW SYSTEM TABLES FOR ENTERPRISE UPGRADE
ALTER TABLE users MODIFY COLUMN role ENUM('user', 'driver', 'admin', 'super_admin', 'hospital', 'dispatcher', 'doctor', 'reception', 'analytics') NOT NULL DEFAULT 'user';
ALTER TABLE users ADD COLUMN hospital_id INT NULL DEFAULT NULL;
ALTER TABLE users ADD CONSTRAINT fk_users_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE SET NULL;
ALTER TABLE hospitals ADD COLUMN available_general_beds INT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS patient_admissions (
    admission_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NULL,
    hospital_id INT NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    patient_phone VARCHAR(20) NULL,
    bed_type ENUM('ICU', 'NICU', 'general') NOT NULL,
    status ENUM('admitted', 'discharged') NOT NULL DEFAULT 'admitted',
    admitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    discharged_at TIMESTAMP NULL DEFAULT NULL,
    doctor_id INT NULL,
    treatment_notes TEXT NULL,
    prescriptions TEXT NULL,
    CONSTRAINT fk_admissions_request FOREIGN KEY (request_id) REFERENCES emergency_requests(request_id) ON DELETE SET NULL,
    CONSTRAINT fk_admissions_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    CONSTRAINT fk_admissions_doctor FOREIGN KEY (doctor_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
