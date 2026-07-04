-- STEP 1: Create and select the database
CREATE DATABASE IF NOT EXISTS bloodbank_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bloodbank_db;
 
-- STEP 2: Drop existing tables (safe to re-run)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS permission_requests;
DROP TABLE IF EXISTS prediction_history;
DROP TABLE IF EXISTS shortage_alerts;
DROP TABLE IF EXISTS shortage_thresholds;
DROP TABLE IF EXISTS blood_requests;
DROP TABLE IF EXISTS blood_inventory;
DROP TABLE IF EXISTS donations;
DROP TABLE IF EXISTS donors;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;
 
-- ─────────────────────────────────────────────
-- USERS
-- ─────────────────────────────────────────────
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    full_name     VARCHAR(100),
    role          ENUM('ADMIN','DONOR','HOSPITAL') NOT NULL DEFAULT 'DONOR',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    is_admin      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
-- ─────────────────────────────────────────────
-- DONORS
-- ─────────────────────────────────────────────
CREATE TABLE donors (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT,
    name               VARCHAR(100) NOT NULL,
    age                INT NOT NULL,
    blood_group        ENUM('A_POS','A_NEG','B_POS','B_NEG','AB_POS','AB_NEG','O_POS','O_NEG') NOT NULL,
    contact_number     VARCHAR(20),
    email              VARCHAR(100),
    city               VARCHAR(100) NOT NULL,
    address            TEXT,
    weight             DOUBLE,
    hemoglobin         DOUBLE,
    bp_systolic        INT,
    bp_diastolic       INT,
    is_pregnant        BOOLEAN DEFAULT FALSE,
    has_recent_illness BOOLEAN DEFAULT FALSE,
    last_donation_date DATE,
    is_eligible        BOOLEAN DEFAULT TRUE,
    eligibility_reason TEXT,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_donor_blood_group (blood_group),
    INDEX idx_donor_city (city)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
-- ─────────────────────────────────────────────
-- DONATIONS
-- ─────────────────────────────────────────────
CREATE TABLE donations (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id               BIGINT NOT NULL,
    blood_group            ENUM('A_POS','A_NEG','B_POS','B_NEG','AB_POS','AB_NEG','O_POS','O_NEG') NOT NULL,
    units_donated          DOUBLE DEFAULT 1.0,
    donation_date          DATE NOT NULL,
    hemoglobin_at_donation DOUBLE,
    bp_systolic            INT,
    bp_diastolic           INT,
    status                 ENUM('COMPLETED','DEFERRED') NOT NULL DEFAULT 'COMPLETED',
    defer_reason           VARCHAR(255),
    notes                  TEXT,
    created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES donors(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
-- ─────────────────────────────────────────────
-- BLOOD INVENTORY
-- ─────────────────────────────────────────────
CREATE TABLE blood_inventory (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_group      ENUM('A_POS','A_NEG','B_POS','B_NEG','AB_POS','AB_NEG','O_POS','O_NEG') NOT NULL,
    units_available  DOUBLE NOT NULL DEFAULT 0,
    collection_date  DATE,
    expiry_date      DATE,
    donation_id      BIGINT,
    batch_number     VARCHAR(50),
    storage_location VARCHAR(100) DEFAULT 'Main Storage',
    status           ENUM('AVAILABLE','RESERVED','ISSUED','EXPIRED','DISCARDED') NOT NULL DEFAULT 'AVAILABLE',
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (donation_id) REFERENCES donations(id) ON DELETE SET NULL,
    INDEX idx_inv_blood_group (blood_group),
    INDEX idx_inv_expiry (expiry_date),
    INDEX idx_inv_status (status)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
-- ─────────────────────────────────────────────
-- SHORTAGE THRESHOLDS
-- ─────────────────────────────────────────────
CREATE TABLE shortage_thresholds (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_group     ENUM('A_POS','A_NEG','B_POS','B_NEG','AB_POS','AB_NEG','O_POS','O_NEG') NOT NULL UNIQUE,
    threshold_units DOUBLE NOT NULL DEFAULT 10.0,
    critical_units  DOUBLE NOT NULL DEFAULT 5.0
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
-- ─────────────────────────────────────────────
-- BLOOD REQUESTS
-- ─────────────────────────────────────────────
CREATE TABLE blood_requests (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_name    VARCHAR(200) NOT NULL,
    hospital_contact VARCHAR(20),
    blood_group      ENUM('A_POS','A_NEG','B_POS','B_NEG','AB_POS','AB_NEG','O_POS','O_NEG') NOT NULL,
    units_required   DOUBLE NOT NULL,
    units_issued     DOUBLE DEFAULT 0,
    request_date     DATE NOT NULL DEFAULT (CURDATE()),
    required_by_date DATE,
    priority         ENUM('NORMAL','URGENT','CRITICAL') NOT NULL DEFAULT 'NORMAL',
    patient_name     VARCHAR(100),
    clinical_reason  TEXT NOT NULL,
    attending_doctor VARCHAR(100),
    status           ENUM('PENDING','APPROVED','REJECTED','ISSUED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    admin_notes      TEXT,
    approved_by      BIGINT,
    approved_at      TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_req_status (status),
    INDEX idx_req_blood_group (blood_group)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
-- ─────────────────────────────────────────────
-- SHORTAGE ALERTS
-- ─────────────────────────────────────────────
CREATE TABLE shortage_alerts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_group     ENUM('A_POS','A_NEG','B_POS','B_NEG','AB_POS','AB_NEG','O_POS','O_NEG') NOT NULL,
    current_units   DOUBLE,
    threshold_units DOUBLE,
    alert_type      ENUM('WARNING','CRITICAL') NOT NULL,
    is_resolved     BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
-- ─────────────────────────────────────────────
-- PREDICTION HISTORY
-- ─────────────────────────────────────────────
CREATE TABLE prediction_history (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_group       ENUM('A_POS','A_NEG','B_POS','B_NEG','AB_POS','AB_NEG','O_POS','O_NEG') NOT NULL,
    prediction_date   DATE NOT NULL,
    predicted_units   DOUBLE NOT NULL,
    actual_units      DOUBLE,
    prediction_method VARCHAR(50) DEFAULT 'MOVING_AVERAGE',
    window_days       INT DEFAULT 30,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
-- ─────────────────────────────────────────────
-- PERMISSION REQUESTS
-- ─────────────────────────────────────────────
CREATE TABLE permission_requests (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    organization VARCHAR(200) NOT NULL,
    reason       TEXT NOT NULL,
    status       ENUM('PENDING','GRANTED','DENIED') NOT NULL DEFAULT 'PENDING',
    reviewed_by  BIGINT,
    reviewed_at  TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
-- ═══════════════════════════════════════════════════════════
-- SEED DATA
-- ═══════════════════════════════════════════════════════════
 
-- Users
-- Passwords: admin=admin123, donor1=donor123, hospital1=hosp123, donor2=donor123, nurse1=donor123
INSERT INTO users (username, password_hash, email, full_name, role, is_active, is_admin) VALUES
('admin',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@hemovault.com',  'System Administrator', 'ADMIN',    TRUE, TRUE),
('donor1',   '$2a$10$8K1p/a0dR1xqM7JHEqeBreZbSp2ZBMnIGX8Q7q3ZUHKTWy.Ei0J7e', 'donor1@email.com',     'Ravi Kumar',           'DONOR',    TRUE, FALSE),
('hospital1','$2a$10$GFvPKMjBwUCq8DKWXM6cZuM1UvCqN1mQLXxkPlRkuJqcXzjyHDjC.', 'apollo@hospital.com',  'Apollo Hospital',      'HOSPITAL', TRUE, FALSE),
('donor2',   '$2a$10$8K1p/a0dR1xqM7JHEqeBreZbSp2ZBMnIGX8Q7q3ZUHKTWy.Ei0J7e', 'donor2@email.com',     'Priya Sharma',         'DONOR',    TRUE, FALSE),
('nurse1',   '$2a$10$8K1p/a0dR1xqM7JHEqeBreZbSp2ZBMnIGX8Q7q3ZUHKTWy.Ei0J7e', 'nurse1@hemovault.com', 'Meena Devi',           'DONOR',    TRUE, FALSE);
 
-- Shortage Thresholds (all 8 blood groups)
INSERT INTO shortage_thresholds (blood_group, threshold_units, critical_units) VALUES
('A_POS', 15.0, 5.0),
('A_NEG', 10.0, 3.0),
('B_POS', 15.0, 5.0),
('B_NEG', 10.0, 3.0),
('AB_POS', 8.0, 3.0),
('AB_NEG', 5.0, 2.0),
('O_POS', 20.0, 8.0),
('O_NEG', 12.0, 4.0);
 
-- Sample Donors
INSERT INTO donors (name, age, blood_group, contact_number, email, city, weight, hemoglobin, bp_systolic, bp_diastolic, is_eligible, eligibility_reason) VALUES
('Ravi Kumar',    28, 'O_POS',  '9876543210', 'ravi@email.com',    'Chennai',    72.0, 14.2, 120, 80, TRUE,  'All criteria met'),
('Priya Sharma',  32, 'A_POS',  '9876543211', 'priya@email.com',   'Mumbai',     58.0, 13.5, 118, 76, TRUE,  'All criteria met'),
('Amit Singh',    45, 'B_NEG',  '9876543212', 'amit@email.com',    'Delhi',      80.0, 15.0, 130, 85, TRUE,  'All criteria met'),
('Sunita Patel',  26, 'AB_POS', '9876543213', 'sunita@email.com',  'Bangalore',  62.0, 12.8, 115, 75, TRUE,  'All criteria met'),
('Kiran Reddy',   38, 'O_NEG',  '9876543214', 'kiran@email.com',   'Hyderabad',  75.0, 13.9, 125, 82, TRUE,  'All criteria met'),
('Meena Devi',    55, 'A_NEG',  '9876543215', 'meena@email.com',   'Chennai',    55.0, 11.9, 140, 88, FALSE, 'Hemoglobin below 12.5 g/dL'),
('Suresh Babu',   22, 'B_POS',  '9876543216', 'suresh@email.com',  'Coimbatore', 90.0, 16.1, 118, 78, TRUE,  'All criteria met'),
('Lakshmi Nair',  35, 'O_POS',  '9876543217', 'lakshmi@email.com', 'Kochi',      65.0, 13.2, 122, 80, TRUE,  'All criteria met');
 
-- Sample Blood Inventory
INSERT INTO blood_inventory (blood_group, units_available, collection_date, expiry_date, batch_number, storage_location, status) VALUES
('O_POS',  8.0, DATE_SUB(CURDATE(), INTERVAL 5  DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'BATCH-001', 'Refrigerator A', 'AVAILABLE'),
('O_POS',  5.0, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY), 'BATCH-002', 'Refrigerator A', 'AVAILABLE'),
('A_POS',  6.0, DATE_SUB(CURDATE(), INTERVAL 3  DAY), DATE_ADD(CURDATE(), INTERVAL 32 DAY), 'BATCH-003', 'Refrigerator B', 'AVAILABLE'),
('A_POS',  4.0, DATE_SUB(CURDATE(), INTERVAL 8  DAY), DATE_ADD(CURDATE(), INTERVAL 27 DAY), 'BATCH-004', 'Refrigerator B', 'AVAILABLE'),
('B_POS',  7.0, DATE_SUB(CURDATE(), INTERVAL 2  DAY), DATE_ADD(CURDATE(), INTERVAL 33 DAY), 'BATCH-005', 'Refrigerator C', 'AVAILABLE'),
('B_NEG',  3.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'BATCH-006', 'Refrigerator C', 'AVAILABLE'),
('AB_POS', 2.0, DATE_SUB(CURDATE(), INTERVAL 7  DAY), DATE_ADD(CURDATE(), INTERVAL 28 DAY), 'BATCH-007', 'Refrigerator D', 'AVAILABLE'),
('AB_NEG', 1.0, DATE_SUB(CURDATE(), INTERVAL 4  DAY), DATE_ADD(CURDATE(), INTERVAL 31 DAY), 'BATCH-008', 'Refrigerator D', 'AVAILABLE'),
('O_NEG',  4.0, DATE_SUB(CURDATE(), INTERVAL 6  DAY), DATE_ADD(CURDATE(), INTERVAL 29 DAY), 'BATCH-009', 'Refrigerator A', 'AVAILABLE'),
('A_NEG',  3.0, DATE_SUB(CURDATE(), INTERVAL 9  DAY), DATE_ADD(CURDATE(), INTERVAL 26 DAY), 'BATCH-010', 'Refrigerator B', 'AVAILABLE'),
('B_POS',  5.0, DATE_SUB(CURDATE(), INTERVAL 1  DAY), DATE_ADD(CURDATE(), INTERVAL 34 DAY), 'BATCH-011', 'Refrigerator C', 'AVAILABLE');
 
-- Sample Blood Requests
INSERT INTO blood_requests (hospital_name, hospital_contact, blood_group, units_required, required_by_date, priority, patient_name, clinical_reason, attending_doctor, status) VALUES
('Apollo Hospital Chennai',  '044-28290290', 'O_POS',  3.0, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'URGENT',   'John Doe',   'Patient scheduled for emergency cardiac bypass surgery requiring O+ blood transfusion due to severe anemia pre-op',          'Dr. Ramesh',  'PENDING'),
('MIOT International',       '044-22490000', 'A_POS',  2.0, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'NORMAL',   'Jane Smith', 'Post-operative transfusion required following hip replacement surgery; patient Hb at 7.2 g/dL',                          'Dr. Priya',   'PENDING'),
('Fortis Malar Hospital',    '044-42892222', 'B_NEG',  1.0, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'CRITICAL', 'Ram Prasad', 'Critical trauma patient admitted with massive internal bleeding from road traffic accident — immediate B- required',        'Dr. Suresh',  'APPROVED'),
('Sri Ramachandra Hospital', '044-45928500', 'AB_POS', 2.0, DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'NORMAL',   'Leela Devi', 'Sickle cell anemia patient requiring scheduled exchange transfusion as per treatment protocol',                           'Dr. Kavitha', 'REJECTED'),
('Kauvery Hospital',         '044-40006000', 'O_NEG',  2.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'URGENT',   'Baby Arjun', 'Premature neonate requiring O- blood for exchange transfusion due to hemolytic disease of newborn', 'Dr. Nair',    'ISSUED');