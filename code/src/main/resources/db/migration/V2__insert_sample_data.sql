-- =========================================================
-- V2: sample data for development and demo
-- The demo password hash comes from the Flyway placeholder
-- ${demopasswordhash} (BCrypt). Set a different hash in production.
-- =========================================================

-- ---------- demo accounts ----------
INSERT INTO users (first_name, last_name, email, password, phone_number, role, active, created_at) VALUES
    ('Admin', 'PetsHotel', 'admin@petshotel.test', '${demopasswordhash}', '0800000001', 'ADMIN', TRUE, NOW()),
    ('Staff', 'PetsHotel', 'staff@petshotel.test', '${demopasswordhash}', '0800000002', 'STAFF', TRUE, NOW());

-- ---------- rooms ----------
INSERT INTO rooms (room_number, name, description, capacity, price_per_pet_per_night, status) VALUES
    ('101', 'Standard', 'ห้องมาตรฐาน มีแอร์และที่นอนนุ่ม', 2, 350.00, 'ACTIVE'),
    ('102', 'Standard', 'ห้องมาตรฐาน มีแอร์และที่นอนนุ่ม', 2, 350.00, 'ACTIVE'),
    ('201', 'Deluxe', 'ห้องกว้าง มีกล้องวงจรปิดให้เจ้าของดูได้', 3, 500.00, 'ACTIVE'),
    ('301', 'Family Suite', 'ห้องใหญ่สำหรับสัตว์หลายตัว มีพื้นที่วิ่งเล่น', 4, 700.00, 'ACTIVE');

-- ---------- extra services ----------
INSERT INTO extra_services (name, description, price, active) VALUES
    ('อาบน้ำ', 'อาบน้ำ เป่าขน และหวีขน', 300.00, TRUE),
    ('พาเดินเล่น', 'พาเดินเล่นนอกห้อง 30 นาที', 150.00, TRUE),
    ('ตัดเล็บ', 'ตัดและตะไบเล็บ', 200.00, TRUE),
    ('ตรวจสุขภาพ', 'สัตวแพทย์ตรวจสุขภาพเบื้องต้น', 500.00, TRUE);

-- ---------- promotions ----------
INSERT INTO promotions (name, description, type, value, start_date, end_date, minimum_nights, active) VALUES
    ('พักยาวลด 10%', 'พักตั้งแต่ 5 คืนขึ้นไป ลด 10%', 'PERCENTAGE', 10.00, DATE '2026-01-01', DATE '2027-12-31', 5, TRUE),
    ('ลด 100 บาท', 'ส่วนลดสำหรับการจองครั้งแรก', 'FIXED_AMOUNT', 100.00, DATE '2026-01-01', DATE '2027-12-31', NULL, TRUE);