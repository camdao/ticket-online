-- Ticket Online - Test Database Schema & Data
-- Database: ticket_online
-- Platform: MySQL 8.0+

-- ============================================
-- 1. USERS TABLE
-- ============================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    username VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================
-- 2. SHOWS TABLE
-- ============================================
CREATE TABLE shows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    start_time DATETIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================
-- 3. SEATS TABLE
-- ============================================
CREATE TABLE seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    show_id BIGINT NOT NULL,
    seat_code VARCHAR(10) NOT NULL,
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (show_id) REFERENCES shows(id),
    UNIQUE KEY unique_seat_per_show (show_id, seat_code)
);

-- ============================================
-- 4. ORDERS TABLE
-- ============================================
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    payment_id BIGINT,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    expire_time DATETIME NOT NULL,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (show_id) REFERENCES shows(id),
    INDEX idx_user_id (user_id),
    INDEX idx_show_id (show_id),
    INDEX idx_status (status)
);

-- ============================================
-- 5. ORDER_SEATS TABLE
-- ============================================
CREATE TABLE order_seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id),
    UNIQUE KEY unique_seat_per_order (order_id, seat_id)
);

-- ============================================
-- 6. PAYMENTS TABLE
-- ============================================
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    provider VARCHAR(50) DEFAULT 'VNPAY',
    amount BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status)
);

-- ============================================
-- TEST DATA - USERS
-- ============================================
-- Password for all users: 123456 (BCrypt encoded)
-- BCrypt hash: $2a$10$R9h7cIPz0gi.URNNP3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm6
INSERT INTO users (email, name, username, password, role) VALUES
('user1@example.com', 'Nguyen Van A', 'user1', '$2a$10$R9h7cIPz0gi.URNNP3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm6', 'USER'),
('user2@example.com', 'Tran Thi B', 'user2', '$2a$10$R9h7cIPz0gi.URNNP3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm6', 'USER'),
('user3@example.com', 'Le Van C', 'user3', '$2a$10$R9h7cIPz0gi.URNNP3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm6', 'USER'),
('user4@example.com', 'Pham Thi D', 'user4', '$2a$10$R9h7cIPz0gi.URNNP3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm6', 'USER'),
('user5@example.com', 'Hoang Van E', 'user5', '$2a$10$R9h7cIPz0gi.URNNP3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm6', 'USER');

-- ============================================
-- TEST DATA - SHOWS
-- ============================================
INSERT INTO shows (name, start_time, location) VALUES
('Concert 2026 - Coldplay', '2026-04-15 19:00:00', 'My Dinh Stadium, Hanoi'),
('Theater Show - Romeo & Juliet', '2026-05-10 20:00:00', 'Hanoi Opera House'),
('Sports Event - Basketball', '2026-03-30 18:00:00', 'Thien Truong Stadium, Nam Dinh'),
('Music Festival 2026', '2026-06-01 17:00:00', 'Lung Linh Park, Ho Chi Minh'),
('Comedy Night', '2026-04-05 19:30:00', 'Lotte Hotel, Hanoi');

-- ============================================
-- TEST DATA - SEATS (for Show 1)
-- ============================================
-- Row A: 10 seats
INSERT INTO seats (show_id, seat_code, status) VALUES
(1, 'A1', 'AVAILABLE'),
(1, 'A2', 'AVAILABLE'),
(1, 'A3', 'AVAILABLE'),
(1, 'A4', 'AVAILABLE'),
(1, 'A5', 'AVAILABLE'),
(1, 'A6', 'AVAILABLE'),
(1, 'A7', 'AVAILABLE'),
(1, 'A8', 'AVAILABLE'),
(1, 'A9', 'AVAILABLE'),
(1, 'A10', 'AVAILABLE');

-- Row B: 10 seats
INSERT INTO seats (show_id, seat_code, status) VALUES
(1, 'B1', 'AVAILABLE'),
(1, 'B2', 'AVAILABLE'),
(1, 'B3', 'AVAILABLE'),
(1, 'B4', 'AVAILABLE'),
(1, 'B5', 'AVAILABLE'),
(1, 'B6', 'AVAILABLE'),
(1, 'B7', 'AVAILABLE'),
(1, 'B8', 'AVAILABLE'),
(1, 'B9', 'AVAILABLE'),
(1, 'B10', 'AVAILABLE');

-- Row C: 10 seats
INSERT INTO seats (show_id, seat_code, status) VALUES
(1, 'C1', 'AVAILABLE'),
(1, 'C2', 'AVAILABLE'),
(1, 'C3', 'AVAILABLE'),
(1, 'C4', 'AVAILABLE'),
(1, 'C5', 'AVAILABLE'),
(1, 'C6', 'AVAILABLE'),
(1, 'C7', 'AVAILABLE'),
(1, 'C8', 'AVAILABLE'),
(1, 'C9', 'AVAILABLE'),
(1, 'C10', 'AVAILABLE');

-- Row D: 10 seats
INSERT INTO seats (show_id, seat_code, status) VALUES
(1, 'D1', 'AVAILABLE'),
(1, 'D2', 'AVAILABLE'),
(1, 'D3', 'AVAILABLE'),
(1, 'D4', 'AVAILABLE'),
(1, 'D5', 'AVAILABLE'),
(1, 'D6', 'AVAILABLE'),
(1, 'D7', 'AVAILABLE'),
(1, 'D8', 'AVAILABLE'),
(1, 'D9', 'AVAILABLE'),
(1, 'D10', 'AVAILABLE');

-- ============================================
-- TEST DATA - SEATS (for Show 2)
-- ============================================
-- Row A: 10 seats
INSERT INTO seats (show_id, seat_code, status) VALUES
(2, 'A1', 'AVAILABLE'),
(2, 'A2', 'AVAILABLE'),
(2, 'A3', 'AVAILABLE'),
(2, 'A4', 'AVAILABLE'),
(2, 'A5', 'AVAILABLE'),
(2, 'A6', 'AVAILABLE'),
(2, 'A7', 'AVAILABLE'),
(2, 'A8', 'AVAILABLE'),
(2, 'A9', 'AVAILABLE'),
(2, 'A10', 'AVAILABLE');

-- Row B: 10 seats
INSERT INTO seats (show_id, seat_code, status) VALUES
(2, 'B1', 'AVAILABLE'),
(2, 'B2', 'AVAILABLE'),
(2, 'B3', 'AVAILABLE'),
(2, 'B4', 'AVAILABLE'),
(2, 'B5', 'AVAILABLE'),
(2, 'B6', 'AVAILABLE'),
(2, 'B7', 'AVAILABLE'),
(2, 'B8', 'AVAILABLE'),
(2, 'B9', 'AVAILABLE'),
(2, 'B10', 'AVAILABLE');

-- ============================================
-- TEST DATA - SEATS (for Show 3)
-- ============================================
-- Row A: 10 seats
INSERT INTO seats (show_id, seat_code, status) VALUES
(3, 'A1', 'AVAILABLE'),
(3, 'A2', 'AVAILABLE'),
(3, 'A3', 'AVAILABLE'),
(3, 'A4', 'AVAILABLE'),
(3, 'A5', 'AVAILABLE'),
(3, 'A6', 'AVAILABLE'),
(3, 'A7', 'AVAILABLE'),
(3, 'A8', 'AVAILABLE'),
(3, 'A9', 'AVAILABLE'),
(3, 'A10', 'AVAILABLE');

-- Row B: 10 seats
INSERT INTO seats (show_id, seat_code, status) VALUES
(3, 'B1', 'AVAILABLE'),
(3, 'B2', 'AVAILABLE'),
(3, 'B3', 'AVAILABLE'),
(3, 'B4', 'AVAILABLE'),
(3, 'B5', 'AVAILABLE'),
(3, 'B6', 'AVAILABLE'),
(3, 'B7', 'AVAILABLE'),
(3, 'B8', 'AVAILABLE'),
(3, 'B9', 'AVAILABLE'),
(3, 'B10', 'AVAILABLE');

-- ============================================
-- TEST DATA - ORDERS (sample orders)
-- ============================================
INSERT INTO orders (user_id, show_id, total_amount, status, expire_time) VALUES
(1, 1, 1000000.00, 'PENDING', DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
(2, 1, 500000.00, 'PENDING', DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
(3, 2, 600000.00, 'PAID', DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
(4, 3, 750000.00, 'PENDING', DATE_ADD(NOW(), INTERVAL 30 MINUTE)),
(5, 1, 1000000.00, 'CANCELLED', DATE_ADD(NOW(), INTERVAL 30 MINUTE));

-- ============================================
-- TEST DATA - ORDER_SEATS (seats in orders)
-- ============================================
-- Order 1: Seats A1, A2 from Show 1
INSERT INTO order_seats (order_id, seat_id) VALUES
(1, 1),
(1, 2);

-- Order 2: Seats B1, B2 from Show 1
INSERT INTO order_seats (order_id, seat_id) VALUES
(2, 11),
(2, 12);

-- Order 3: Seats A1, A2 from Show 2
INSERT INTO order_seats (order_id, seat_id) VALUES
(3, 121),
(3, 122);

-- Order 4: Seats A1, A2 from Show 3
INSERT INTO order_seats (order_id, seat_id) VALUES
(4, 141),
(4, 142);

-- ============================================
-- TEST DATA - PAYMENTS
-- ============================================
INSERT INTO payments (order_id, provider, amount, status) VALUES
(1, 'VNPAY', 100000000, 'PENDING'),
(2, 'VNPAY', 50000000, 'PENDING'),
(3, 'VNPAY', 60000000, 'SUCCESS'),
(4, 'VNPAY', 75000000, 'PENDING'),
(5, 'VNPAY', 100000000, 'FAILED');

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_show_id ON orders(show_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_seats_show_id ON seats(show_id);
CREATE INDEX idx_seats_status ON seats(status);

-- ============================================
-- VERIFY DATA
-- ============================================
SELECT 'Users created:' as info, COUNT(*) as count FROM users;
SELECT 'Shows created:' as info, COUNT(*) as count FROM shows;
SELECT 'Seats created:' as info, COUNT(*) as count FROM seats;
SELECT 'Orders created:' as info, COUNT(*) as count FROM orders;
SELECT 'Payments created:' as info, COUNT(*) as count FROM payments;
