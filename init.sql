-- ============================================
-- Database Initialization Script
-- Movie Ticket Booking System
-- MySQL Database
-- ============================================

-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS booking_details;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS showtimes;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS cinemas;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS users;

-- ============================================
-- Table: users
-- Description: User account information
-- ============================================
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role ENUM('ROLE_USER', 'ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: movies
-- Description: Movie information
-- ============================================
CREATE TABLE movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    duration INT NOT NULL COMMENT 'Duration in minutes',
    description TEXT,
    image_url VARCHAR(1000),
    trailer_url VARCHAR(1000),
    release_date DATE,
    genre VARCHAR(255),
    director VARCHAR(255),
    cast TEXT,
    rating VARCHAR(10) COMMENT 'Age rating (e.g., P, C13, C16, C18)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: cinemas
-- Description: Cinema locations with brand information
-- ============================================
CREATE TABLE cinemas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Cinema name (e.g., CGV Vincom Center)',
    brand VARCHAR(100) NOT NULL COMMENT 'Cinema brand/chain (e.g., CGV, Lotte Cinema, Galaxy Cinema)',
    logo_url VARCHAR(1000),
    address VARCHAR(500),
    district VARCHAR(100),
    city VARCHAR(100),
    phone VARCHAR(20),
    website VARCHAR(500),
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_brand (brand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: rooms
-- Description: Screening rooms in a cinema
-- ============================================
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cinema_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT 'e.g., Room 1, IMAX, VIP Room',
    capacity INT NOT NULL DEFAULT 0 COMMENT 'Total number of seats',
    room_type VARCHAR(50) COMMENT 'e.g., Standard, IMAX, 4DX, VIP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cinema_id) REFERENCES cinemas(id) ON DELETE CASCADE,
    INDEX idx_cinema_id (cinema_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: seats
-- Description: Seat inventory in each room
-- ============================================
CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    row_label VARCHAR(5) NOT NULL COMMENT 'e.g., A, B, C',
    seat_number INT NOT NULL COMMENT 'Seat number in the row',
    seat_type ENUM('REGULAR', 'VIP', 'COUPLE') NOT NULL DEFAULT 'REGULAR',
    surcharge DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Additional price for special seats',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Soft delete flag',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE KEY uk_room_seat (room_id, row_label, seat_number),
    INDEX idx_room_id (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: showtimes
-- Description: Movie screening schedules
-- ============================================
CREATE TABLE showtimes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    cinema_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    base_price DECIMAL(10,2) NOT NULL COMMENT 'Base ticket price',
    status ENUM('ACTIVE', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (cinema_id) REFERENCES cinemas(id) ON DELETE CASCADE,
    INDEX idx_movie_id (movie_id),
    INDEX idx_room_id (room_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: bookings
-- Description: Booking orders
-- ============================================
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at DATETIME NULL,
    expires_at DATETIME NULL,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_showtime_id (showtime_id),
    INDEX idx_booking_code (booking_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: booking_details
-- Description: Seats in a booking order
-- ============================================
CREATE TABLE booking_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL COMMENT 'Price snapshot at booking time',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE,
    UNIQUE KEY uk_booking_seat (booking_id, seat_id),
    INDEX idx_booking_id (booking_id),
    INDEX idx_seat_id (seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: payments
-- Description: Payment transactions
-- ============================================
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    payment_method ENUM('VNPAY', 'MOMO', 'CASH', 'CARD') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    paid_at DATETIME NULL,
    payment_url VARCHAR(2000),
    gateway_response TEXT COMMENT 'JSON response from payment gateway',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    INDEX idx_booking_id (booking_id),
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Sample Data
-- ============================================

-- Insert sample users
INSERT INTO users (username, email, password, full_name, phone_number, role) VALUES
('admin', 'admin@example.com', '$2a$10$dummyHashedPassword1', 'Admin User', '0901234567', 'ROLE_ADMIN'),
('johndoe', 'john.doe@example.com', '$2a$10$dummyHashedPassword2', 'John Doe', '0912345678', 'ROLE_USER'),
('janesmith', 'jane.smith@example.com', '$2a$10$dummyHashedPassword3', 'Jane Smith', '0923456789', 'ROLE_USER');

-- Insert sample movies
INSERT INTO movies (title, duration, description, image_url, release_date, genre, rating) VALUES
('The Shawshank Redemption', 142, 'Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.', 'https://example.com/shawshank.jpg', '1994-09-23', 'Drama', 'C16'),
('The Godfather', 175, 'The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.', 'https://example.com/godfather.jpg', '1972-03-24', 'Crime, Drama', 'C18'),
('The Dark Knight', 152, 'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests.', 'https://example.com/darkknight.jpg', '2008-07-18', 'Action, Crime, Drama', 'C13');

-- Insert sample cinemas
INSERT INTO cinemas (brand, name, logo_url, address, district, city, phone, website, description) VALUES
-- CGV Cinemas
('CGV', 'CGV Vincom Center', 'https://example.com/cgv-logo.png', '72 Le Thanh Ton, District 1', 'District 1', 'Ho Chi Minh City', '1900-6017', 'https://cgv.vn', 'Leading cinema chain in Vietnam'),
('CGV', 'CGV Aeon Mall', 'https://example.com/cgv-logo.png', '30 Bo Bao Tan Thang, Son Ky', 'Tan Phu District', 'Ho Chi Minh City', '1900-6017', 'https://cgv.vn', 'Leading cinema chain in Vietnam'),
-- Lotte Cinema
('Lotte Cinema', 'Lotte Cinema Diamond Plaza', 'https://example.com/lotte-logo.png', '34 Le Duan, District 1', 'District 1', 'Ho Chi Minh City', '1900-6520', 'https://lottecinema.com.vn', 'Premium cinema experience'),
('Lotte Cinema', 'Lotte Cinema Landmark 81', 'https://example.com/lotte-logo.png', '208 Nguyen Huu Canh, Ward 22', 'Binh Thanh District', 'Ho Chi Minh City', '1900-6520', 'https://lottecinema.com.vn', 'Premium cinema experience'),
-- Galaxy Cinema
('Galaxy Cinema', 'Galaxy Nguyen Du', 'https://example.com/galaxy-logo.png', '116 Nguyen Du, District 1', 'District 1', 'Ho Chi Minh City', '1900-2224', 'https://galaxycine.vn', 'Modern cinema with latest technology'),
('Galaxy Cinema', 'Galaxy Tan Binh', 'https://example.com/galaxy-logo.png', '246 Nguyen Hong Dao, Ward 13', 'Tan Binh District', 'Ho Chi Minh City', '1900-2224', 'https://galaxycine.vn', 'Modern cinema with latest technology');

-- Insert sample rooms
INSERT INTO rooms (cinema_id, name, capacity, room_type) VALUES
-- CGV Vincom Center (cinema_id=1)
(1, 'Room 1', 50, 'Standard'),
(1, 'Room 2', 60, 'Standard'),
(1, 'IMAX Room', 120, 'IMAX'),
-- CGV Aeon Mall (cinema_id=2)
(2, 'Room A', 45, 'Standard'),
(2, 'VIP Room', 30, 'VIP'),
-- Lotte Cinema Diamond Plaza (cinema_id=3)
(3, '4DX Room', 60, '4DX'),
(3, 'Standard Room', 55, 'Standard'),
-- Lotte Cinema Landmark 81 (cinema_id=4)
(4, 'Premium Room', 80, 'VIP'),
-- Galaxy Nguyen Du (cinema_id=5)
(5, 'Room 1', 70, 'Standard'),
(5, 'Room 2', 50, 'Standard'),
-- Galaxy Tan Binh (cinema_id=6)
(6, 'Room A', 60, 'Standard');

-- Insert sample seats for Room 1 (room_id=1)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Row A (Regular seats)
(1, 'A', 1, 'REGULAR', 0.00),
(1, 'A', 2, 'REGULAR', 0.00),
(1, 'A', 3, 'REGULAR', 0.00),
(1, 'A', 4, 'REGULAR', 0.00),
(1, 'A', 5, 'REGULAR', 0.00),
(1, 'A', 6, 'REGULAR', 0.00),
(1, 'A', 7, 'REGULAR', 0.00),
(1, 'A', 8, 'REGULAR', 0.00),
(1, 'A', 9, 'REGULAR', 0.00),
(1, 'A', 10, 'REGULAR', 0.00),
-- Row B (VIP seats with surcharge)
(1, 'B', 1, 'VIP', 20000.00),
(1, 'B', 2, 'VIP', 20000.00),
(1, 'B', 3, 'VIP', 20000.00),
(1, 'B', 4, 'VIP', 20000.00),
(1, 'B', 5, 'VIP', 20000.00),
(1, 'B', 6, 'VIP', 20000.00),
(1, 'B', 7, 'VIP', 20000.00),
(1, 'B', 8, 'VIP', 20000.00),
(1, 'B', 9, 'VIP', 20000.00),
(1, 'B', 10, 'VIP', 20000.00),
-- Row C (Couple seats with surcharge)
(1, 'C', 1, 'COUPLE', 30000.00),
(1, 'C', 2, 'COUPLE', 30000.00),
(1, 'C', 3, 'COUPLE', 30000.00),
(1, 'C', 4, 'COUPLE', 30000.00),
(1, 'C', 5, 'COUPLE', 30000.00),
-- Row D (Regular seats)
(1, 'D', 1, 'REGULAR', 0.00),
(1, 'D', 2, 'REGULAR', 0.00),
(1, 'D', 3, 'REGULAR', 0.00),
(1, 'D', 4, 'REGULAR', 0.00),
(1, 'D', 5, 'REGULAR', 0.00),
(1, 'D', 6, 'REGULAR', 0.00),
(1, 'D', 7, 'REGULAR', 0.00),
(1, 'D', 8, 'REGULAR', 0.00),
(1, 'D', 9, 'REGULAR', 0.00),
(1, 'D', 10, 'REGULAR', 0.00),
-- Row E (Regular seats)
(1, 'E', 1, 'REGULAR', 0.00),
(1, 'E', 2, 'REGULAR', 0.00),
(1, 'E', 3, 'REGULAR', 0.00),
(1, 'E', 4, 'REGULAR', 0.00),
(1, 'E', 5, 'REGULAR', 0.00),
(1, 'E', 6, 'REGULAR', 0.00),
(1, 'E', 7, 'REGULAR', 0.00),
(1, 'E', 8, 'REGULAR', 0.00),
(1, 'E', 9, 'REGULAR', 0.00),
(1, 'E', 10, 'REGULAR', 0.00);

-- Insert seats for Room 2 (room_id=2, 60 seats, Standard)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Row A (Regular seats)
(2, 'A', 1, 'REGULAR', 0.00), (2, 'A', 2, 'REGULAR', 0.00), (2, 'A', 3, 'REGULAR', 0.00),
(2, 'A', 4, 'REGULAR', 0.00), (2, 'A', 5, 'REGULAR', 0.00), (2, 'A', 6, 'REGULAR', 0.00),
(2, 'A', 7, 'REGULAR', 0.00), (2, 'A', 8, 'REGULAR', 0.00), (2, 'A', 9, 'REGULAR', 0.00),
(2, 'A', 10, 'REGULAR', 0.00),
-- Row B (Regular seats)
(2, 'B', 1, 'REGULAR', 0.00), (2, 'B', 2, 'REGULAR', 0.00), (2, 'B', 3, 'REGULAR', 0.00),
(2, 'B', 4, 'REGULAR', 0.00), (2, 'B', 5, 'REGULAR', 0.00), (2, 'B', 6, 'REGULAR', 0.00),
(2, 'B', 7, 'REGULAR', 0.00), (2, 'B', 8, 'REGULAR', 0.00), (2, 'B', 9, 'REGULAR', 0.00),
(2, 'B', 10, 'REGULAR', 0.00),
-- Row C (VIP seats)
(2, 'C', 1, 'VIP', 20000.00), (2, 'C', 2, 'VIP', 20000.00), (2, 'C', 3, 'VIP', 20000.00),
(2, 'C', 4, 'VIP', 20000.00), (2, 'C', 5, 'VIP', 20000.00), (2, 'C', 6, 'VIP', 20000.00),
(2, 'C', 7, 'VIP', 20000.00), (2, 'C', 8, 'VIP', 20000.00), (2, 'C', 9, 'VIP', 20000.00),
(2, 'C', 10, 'VIP', 20000.00),
-- Row D (VIP seats)
(2, 'D', 1, 'VIP', 20000.00), (2, 'D', 2, 'VIP', 20000.00), (2, 'D', 3, 'VIP', 20000.00),
(2, 'D', 4, 'VIP', 20000.00), (2, 'D', 5, 'VIP', 20000.00), (2, 'D', 6, 'VIP', 20000.00),
(2, 'D', 7, 'VIP', 20000.00), (2, 'D', 8, 'VIP', 20000.00), (2, 'D', 9, 'VIP', 20000.00),
(2, 'D', 10, 'VIP', 20000.00),
-- Row E (Regular seats)
(2, 'E', 1, 'REGULAR', 0.00), (2, 'E', 2, 'REGULAR', 0.00), (2, 'E', 3, 'REGULAR', 0.00),
(2, 'E', 4, 'REGULAR', 0.00), (2, 'E', 5, 'REGULAR', 0.00), (2, 'E', 6, 'REGULAR', 0.00),
(2, 'E', 7, 'REGULAR', 0.00), (2, 'E', 8, 'REGULAR', 0.00), (2, 'E', 9, 'REGULAR', 0.00),
(2, 'E', 10, 'REGULAR', 0.00),
-- Row F (Regular seats)
(2, 'F', 1, 'REGULAR', 0.00), (2, 'F', 2, 'REGULAR', 0.00), (2, 'F', 3, 'REGULAR', 0.00),
(2, 'F', 4, 'REGULAR', 0.00), (2, 'F', 5, 'REGULAR', 0.00), (2, 'F', 6, 'REGULAR', 0.00),
(2, 'F', 7, 'REGULAR', 0.00), (2, 'F', 8, 'REGULAR', 0.00), (2, 'F', 9, 'REGULAR', 0.00),
(2, 'F', 10, 'REGULAR', 0.00);

-- Insert seats for Room 3 (room_id=3, 120 seats, IMAX)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Rows A-C (Regular seats - 36 total)
(3, 'A', 1, 'REGULAR', 0.00), (3, 'A', 2, 'REGULAR', 0.00), (3, 'A', 3, 'REGULAR', 0.00),
(3, 'A', 4, 'REGULAR', 0.00), (3, 'A', 5, 'REGULAR', 0.00), (3, 'A', 6, 'REGULAR', 0.00),
(3, 'A', 7, 'REGULAR', 0.00), (3, 'A', 8, 'REGULAR', 0.00), (3, 'A', 9, 'REGULAR', 0.00),
(3, 'A', 10, 'REGULAR', 0.00), (3, 'A', 11, 'REGULAR', 0.00), (3, 'A', 12, 'REGULAR', 0.00),
(3, 'B', 1, 'REGULAR', 0.00), (3, 'B', 2, 'REGULAR', 0.00), (3, 'B', 3, 'REGULAR', 0.00),
(3, 'B', 4, 'REGULAR', 0.00), (3, 'B', 5, 'REGULAR', 0.00), (3, 'B', 6, 'REGULAR', 0.00),
(3, 'B', 7, 'REGULAR', 0.00), (3, 'B', 8, 'REGULAR', 0.00), (3, 'B', 9, 'REGULAR', 0.00),
(3, 'B', 10, 'REGULAR', 0.00), (3, 'B', 11, 'REGULAR', 0.00), (3, 'B', 12, 'REGULAR', 0.00),
(3, 'C', 1, 'REGULAR', 0.00), (3, 'C', 2, 'REGULAR', 0.00), (3, 'C', 3, 'REGULAR', 0.00),
(3, 'C', 4, 'REGULAR', 0.00), (3, 'C', 5, 'REGULAR', 0.00), (3, 'C', 6, 'REGULAR', 0.00),
(3, 'C', 7, 'REGULAR', 0.00), (3, 'C', 8, 'REGULAR', 0.00), (3, 'C', 9, 'REGULAR', 0.00),
(3, 'C', 10, 'REGULAR', 0.00), (3, 'C', 11, 'REGULAR', 0.00), (3, 'C', 12, 'REGULAR', 0.00),
-- Rows D-E (VIP seats - 24 total)
(3, 'D', 1, 'VIP', 30000.00), (3, 'D', 2, 'VIP', 30000.00), (3, 'D', 3, 'VIP', 30000.00),
(3, 'D', 4, 'VIP', 30000.00), (3, 'D', 5, 'VIP', 30000.00), (3, 'D', 6, 'VIP', 30000.00),
(3, 'D', 7, 'VIP', 30000.00), (3, 'D', 8, 'VIP', 30000.00), (3, 'D', 9, 'VIP', 30000.00),
(3, 'D', 10, 'VIP', 30000.00), (3, 'D', 11, 'VIP', 30000.00), (3, 'D', 12, 'VIP', 30000.00),
(3, 'E', 1, 'VIP', 30000.00), (3, 'E', 2, 'VIP', 30000.00), (3, 'E', 3, 'VIP', 30000.00),
(3, 'E', 4, 'VIP', 30000.00), (3, 'E', 5, 'VIP', 30000.00), (3, 'E', 6, 'VIP', 30000.00),
(3, 'E', 7, 'VIP', 30000.00), (3, 'E', 8, 'VIP', 30000.00), (3, 'E', 9, 'VIP', 30000.00),
(3, 'E', 10, 'VIP', 30000.00), (3, 'E', 11, 'VIP', 30000.00), (3, 'E', 12, 'VIP', 30000.00),
-- Row F (Couple seats - 12 total)
(3, 'F', 1, 'COUPLE', 40000.00), (3, 'F', 2, 'COUPLE', 40000.00), (3, 'F', 3, 'COUPLE', 40000.00),
(3, 'F', 4, 'COUPLE', 40000.00), (3, 'F', 5, 'COUPLE', 40000.00), (3, 'F', 6, 'COUPLE', 40000.00),
(3, 'F', 7, 'COUPLE', 40000.00), (3, 'F', 8, 'COUPLE', 40000.00), (3, 'F', 9, 'COUPLE', 40000.00),
(3, 'F', 10, 'COUPLE', 40000.00), (3, 'F', 11, 'COUPLE', 40000.00), (3, 'F', 12, 'COUPLE', 40000.00),
-- Rows G-J (Regular seats - 48 total)
(3, 'G', 1, 'REGULAR', 0.00), (3, 'G', 2, 'REGULAR', 0.00), (3, 'G', 3, 'REGULAR', 0.00),
(3, 'G', 4, 'REGULAR', 0.00), (3, 'G', 5, 'REGULAR', 0.00), (3, 'G', 6, 'REGULAR', 0.00),
(3, 'G', 7, 'REGULAR', 0.00), (3, 'G', 8, 'REGULAR', 0.00), (3, 'G', 9, 'REGULAR', 0.00),
(3, 'G', 10, 'REGULAR', 0.00), (3, 'G', 11, 'REGULAR', 0.00), (3, 'G', 12, 'REGULAR', 0.00),
(3, 'H', 1, 'REGULAR', 0.00), (3, 'H', 2, 'REGULAR', 0.00), (3, 'H', 3, 'REGULAR', 0.00),
(3, 'H', 4, 'REGULAR', 0.00), (3, 'H', 5, 'REGULAR', 0.00), (3, 'H', 6, 'REGULAR', 0.00),
(3, 'H', 7, 'REGULAR', 0.00), (3, 'H', 8, 'REGULAR', 0.00), (3, 'H', 9, 'REGULAR', 0.00),
(3, 'H', 10, 'REGULAR', 0.00), (3, 'H', 11, 'REGULAR', 0.00), (3, 'H', 12, 'REGULAR', 0.00),
(3, 'I', 1, 'REGULAR', 0.00), (3, 'I', 2, 'REGULAR', 0.00), (3, 'I', 3, 'REGULAR', 0.00),
(3, 'I', 4, 'REGULAR', 0.00), (3, 'I', 5, 'REGULAR', 0.00), (3, 'I', 6, 'REGULAR', 0.00),
(3, 'I', 7, 'REGULAR', 0.00), (3, 'I', 8, 'REGULAR', 0.00), (3, 'I', 9, 'REGULAR', 0.00),
(3, 'I', 10, 'REGULAR', 0.00), (3, 'I', 11, 'REGULAR', 0.00), (3, 'I', 12, 'REGULAR', 0.00),
(3, 'J', 1, 'REGULAR', 0.00), (3, 'J', 2, 'REGULAR', 0.00), (3, 'J', 3, 'REGULAR', 0.00),
(3, 'J', 4, 'REGULAR', 0.00), (3, 'J', 5, 'REGULAR', 0.00), (3, 'J', 6, 'REGULAR', 0.00),
(3, 'J', 7, 'REGULAR', 0.00), (3, 'J', 8, 'REGULAR', 0.00), (3, 'J', 9, 'REGULAR', 0.00),
(3, 'J', 10, 'REGULAR', 0.00), (3, 'J', 11, 'REGULAR', 0.00), (3, 'J', 12, 'REGULAR', 0.00);

-- Insert seats for Room 4 (room_id=4, 45 seats, Standard)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Row A (Regular - 9 seats)
(4, 'A', 1, 'REGULAR', 0.00), (4, 'A', 2, 'REGULAR', 0.00), (4, 'A', 3, 'REGULAR', 0.00),
(4, 'A', 4, 'REGULAR', 0.00), (4, 'A', 5, 'REGULAR', 0.00), (4, 'A', 6, 'REGULAR', 0.00),
(4, 'A', 7, 'REGULAR', 0.00), (4, 'A', 8, 'REGULAR', 0.00), (4, 'A', 9, 'REGULAR', 0.00),
-- Row B (VIP - 9 seats)
(4, 'B', 1, 'VIP', 20000.00), (4, 'B', 2, 'VIP', 20000.00), (4, 'B', 3, 'VIP', 20000.00),
(4, 'B', 4, 'VIP', 20000.00), (4, 'B', 5, 'VIP', 20000.00), (4, 'B', 6, 'VIP', 20000.00),
(4, 'B', 7, 'VIP', 20000.00), (4, 'B', 8, 'VIP', 20000.00), (4, 'B', 9, 'VIP', 20000.00),
-- Row C (Couple - 9 seats)
(4, 'C', 1, 'COUPLE', 30000.00), (4, 'C', 2, 'COUPLE', 30000.00), (4, 'C', 3, 'COUPLE', 30000.00),
(4, 'C', 4, 'COUPLE', 30000.00), (4, 'C', 5, 'COUPLE', 30000.00), (4, 'C', 6, 'COUPLE', 30000.00),
(4, 'C', 7, 'COUPLE', 30000.00), (4, 'C', 8, 'COUPLE', 30000.00), (4, 'C', 9, 'COUPLE', 30000.00),
-- Row D (Regular - 9 seats)
(4, 'D', 1, 'REGULAR', 0.00), (4, 'D', 2, 'REGULAR', 0.00), (4, 'D', 3, 'REGULAR', 0.00),
(4, 'D', 4, 'REGULAR', 0.00), (4, 'D', 5, 'REGULAR', 0.00), (4, 'D', 6, 'REGULAR', 0.00),
(4, 'D', 7, 'REGULAR', 0.00), (4, 'D', 8, 'REGULAR', 0.00), (4, 'D', 9, 'REGULAR', 0.00),
-- Row E (Regular - 9 seats)
(4, 'E', 1, 'REGULAR', 0.00), (4, 'E', 2, 'REGULAR', 0.00), (4, 'E', 3, 'REGULAR', 0.00),
(4, 'E', 4, 'REGULAR', 0.00), (4, 'E', 5, 'REGULAR', 0.00), (4, 'E', 6, 'REGULAR', 0.00),
(4, 'E', 7, 'REGULAR', 0.00), (4, 'E', 8, 'REGULAR', 0.00), (4, 'E', 9, 'REGULAR', 0.00);

-- Insert seats for Room 5 (room_id=5, 30 seats, VIP)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Row A (VIP - 8 seats)
(5, 'A', 1, 'VIP', 40000.00), (5, 'A', 2, 'VIP', 40000.00), (5, 'A', 3, 'VIP', 40000.00),
(5, 'A', 4, 'VIP', 40000.00), (5, 'A', 5, 'VIP', 40000.00), (5, 'A', 6, 'VIP', 40000.00),
(5, 'A', 7, 'VIP', 40000.00), (5, 'A', 8, 'VIP', 40000.00),
-- Row B (VIP - 8 seats)
(5, 'B', 1, 'VIP', 40000.00), (5, 'B', 2, 'VIP', 40000.00), (5, 'B', 3, 'VIP', 40000.00),
(5, 'B', 4, 'VIP', 40000.00), (5, 'B', 5, 'VIP', 40000.00), (5, 'B', 6, 'VIP', 40000.00),
(5, 'B', 7, 'VIP', 40000.00), (5, 'B', 8, 'VIP', 40000.00),
-- Row C (Couple - 6 seats)
(5, 'C', 1, 'COUPLE', 60000.00), (5, 'C', 2, 'COUPLE', 60000.00), (5, 'C', 3, 'COUPLE', 60000.00),
(5, 'C', 4, 'COUPLE', 60000.00), (5, 'C', 5, 'COUPLE', 60000.00), (5, 'C', 6, 'COUPLE', 60000.00),
-- Row D (VIP - 8 seats)
(5, 'D', 1, 'VIP', 40000.00), (5, 'D', 2, 'VIP', 40000.00), (5, 'D', 3, 'VIP', 40000.00),
(5, 'D', 4, 'VIP', 40000.00), (5, 'D', 5, 'VIP', 40000.00), (5, 'D', 6, 'VIP', 40000.00),
(5, 'D', 7, 'VIP', 40000.00), (5, 'D', 8, 'VIP', 40000.00);

-- Insert seats for Room 6 (room_id=6, 60 seats, 4DX)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- All rows have 4DX surcharge (special motion seats)
-- Row A
(6, 'A', 1, 'REGULAR', 50000.00), (6, 'A', 2, 'REGULAR', 50000.00), (6, 'A', 3, 'REGULAR', 50000.00),
(6, 'A', 4, 'REGULAR', 50000.00), (6, 'A', 5, 'REGULAR', 50000.00), (6, 'A', 6, 'REGULAR', 50000.00),
(6, 'A', 7, 'REGULAR', 50000.00), (6, 'A', 8, 'REGULAR', 50000.00), (6, 'A', 9, 'REGULAR', 50000.00),
(6, 'A', 10, 'REGULAR', 50000.00),
-- Row B
(6, 'B', 1, 'REGULAR', 50000.00), (6, 'B', 2, 'REGULAR', 50000.00), (6, 'B', 3, 'REGULAR', 50000.00),
(6, 'B', 4, 'REGULAR', 50000.00), (6, 'B', 5, 'REGULAR', 50000.00), (6, 'B', 6, 'REGULAR', 50000.00),
(6, 'B', 7, 'REGULAR', 50000.00), (6, 'B', 8, 'REGULAR', 50000.00), (6, 'B', 9, 'REGULAR', 50000.00),
(6, 'B', 10, 'REGULAR', 50000.00),
-- Row C
(6, 'C', 1, 'REGULAR', 50000.00), (6, 'C', 2, 'REGULAR', 50000.00), (6, 'C', 3, 'REGULAR', 50000.00),
(6, 'C', 4, 'REGULAR', 50000.00), (6, 'C', 5, 'REGULAR', 50000.00), (6, 'C', 6, 'REGULAR', 50000.00),
(6, 'C', 7, 'REGULAR', 50000.00), (6, 'C', 8, 'REGULAR', 50000.00), (6, 'C', 9, 'REGULAR', 50000.00),
(6, 'C', 10, 'REGULAR', 50000.00),
-- Row D
(6, 'D', 1, 'REGULAR', 50000.00), (6, 'D', 2, 'REGULAR', 50000.00), (6, 'D', 3, 'REGULAR', 50000.00),
(6, 'D', 4, 'REGULAR', 50000.00), (6, 'D', 5, 'REGULAR', 50000.00), (6, 'D', 6, 'REGULAR', 50000.00),
(6, 'D', 7, 'REGULAR', 50000.00), (6, 'D', 8, 'REGULAR', 50000.00), (6, 'D', 9, 'REGULAR', 50000.00),
(6, 'D', 10, 'REGULAR', 50000.00),
-- Row E
(6, 'E', 1, 'REGULAR', 50000.00), (6, 'E', 2, 'REGULAR', 50000.00), (6, 'E', 3, 'REGULAR', 50000.00),
(6, 'E', 4, 'REGULAR', 50000.00), (6, 'E', 5, 'REGULAR', 50000.00), (6, 'E', 6, 'REGULAR', 50000.00),
(6, 'E', 7, 'REGULAR', 50000.00), (6, 'E', 8, 'REGULAR', 50000.00), (6, 'E', 9, 'REGULAR', 50000.00),
(6, 'E', 10, 'REGULAR', 50000.00),
-- Row F
(6, 'F', 1, 'REGULAR', 50000.00), (6, 'F', 2, 'REGULAR', 50000.00), (6, 'F', 3, 'REGULAR', 50000.00),
(6, 'F', 4, 'REGULAR', 50000.00), (6, 'F', 5, 'REGULAR', 50000.00), (6, 'F', 6, 'REGULAR', 50000.00),
(6, 'F', 7, 'REGULAR', 50000.00), (6, 'F', 8, 'REGULAR', 50000.00), (6, 'F', 9, 'REGULAR', 50000.00),
(6, 'F', 10, 'REGULAR', 50000.00);

-- Insert seats for Room 7 (room_id=7, 55 seats, Standard)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Row A (Regular - 11 seats)
(7, 'A', 1, 'REGULAR', 0.00), (7, 'A', 2, 'REGULAR', 0.00), (7, 'A', 3, 'REGULAR', 0.00),
(7, 'A', 4, 'REGULAR', 0.00), (7, 'A', 5, 'REGULAR', 0.00), (7, 'A', 6, 'REGULAR', 0.00),
(7, 'A', 7, 'REGULAR', 0.00), (7, 'A', 8, 'REGULAR', 0.00), (7, 'A', 9, 'REGULAR', 0.00),
(7, 'A', 10, 'REGULAR', 0.00), (7, 'A', 11, 'REGULAR', 0.00),
-- Row B (Regular - 11 seats)
(7, 'B', 1, 'REGULAR', 0.00), (7, 'B', 2, 'REGULAR', 0.00), (7, 'B', 3, 'REGULAR', 0.00),
(7, 'B', 4, 'REGULAR', 0.00), (7, 'B', 5, 'REGULAR', 0.00), (7, 'B', 6, 'REGULAR', 0.00),
(7, 'B', 7, 'REGULAR', 0.00), (7, 'B', 8, 'REGULAR', 0.00), (7, 'B', 9, 'REGULAR', 0.00),
(7, 'B', 10, 'REGULAR', 0.00), (7, 'B', 11, 'REGULAR', 0.00),
-- Row C (VIP - 11 seats)
(7, 'C', 1, 'VIP', 20000.00), (7, 'C', 2, 'VIP', 20000.00), (7, 'C', 3, 'VIP', 20000.00),
(7, 'C', 4, 'VIP', 20000.00), (7, 'C', 5, 'VIP', 20000.00), (7, 'C', 6, 'VIP', 20000.00),
(7, 'C', 7, 'VIP', 20000.00), (7, 'C', 8, 'VIP', 20000.00), (7, 'C', 9, 'VIP', 20000.00),
(7, 'C', 10, 'VIP', 20000.00), (7, 'C', 11, 'VIP', 20000.00),
-- Row D (VIP - 11 seats)
(7, 'D', 1, 'VIP', 20000.00), (7, 'D', 2, 'VIP', 20000.00), (7, 'D', 3, 'VIP', 20000.00),
(7, 'D', 4, 'VIP', 20000.00), (7, 'D', 5, 'VIP', 20000.00), (7, 'D', 6, 'VIP', 20000.00),
(7, 'D', 7, 'VIP', 20000.00), (7, 'D', 8, 'VIP', 20000.00), (7, 'D', 9, 'VIP', 20000.00),
(7, 'D', 10, 'VIP', 20000.00), (7, 'D', 11, 'VIP', 20000.00),
-- Row E (Regular - 11 seats)
(7, 'E', 1, 'REGULAR', 0.00), (7, 'E', 2, 'REGULAR', 0.00), (7, 'E', 3, 'REGULAR', 0.00),
(7, 'E', 4, 'REGULAR', 0.00), (7, 'E', 5, 'REGULAR', 0.00), (7, 'E', 6, 'REGULAR', 0.00),
(7, 'E', 7, 'REGULAR', 0.00), (7, 'E', 8, 'REGULAR', 0.00), (7, 'E', 9, 'REGULAR', 0.00),
(7, 'E', 10, 'REGULAR', 0.00), (7, 'E', 11, 'REGULAR', 0.00);

-- Insert seats for Room 8 (room_id=8, 80 seats, VIP Premium)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Rows A-D (VIP - 40 seats total)
(8, 'A', 1, 'VIP', 40000.00), (8, 'A', 2, 'VIP', 40000.00), (8, 'A', 3, 'VIP', 40000.00),
(8, 'A', 4, 'VIP', 40000.00), (8, 'A', 5, 'VIP', 40000.00), (8, 'A', 6, 'VIP', 40000.00),
(8, 'A', 7, 'VIP', 40000.00), (8, 'A', 8, 'VIP', 40000.00), (8, 'A', 9, 'VIP', 40000.00),
(8, 'A', 10, 'VIP', 40000.00),
(8, 'B', 1, 'VIP', 40000.00), (8, 'B', 2, 'VIP', 40000.00), (8, 'B', 3, 'VIP', 40000.00),
(8, 'B', 4, 'VIP', 40000.00), (8, 'B', 5, 'VIP', 40000.00), (8, 'B', 6, 'VIP', 40000.00),
(8, 'B', 7, 'VIP', 40000.00), (8, 'B', 8, 'VIP', 40000.00), (8, 'B', 9, 'VIP', 40000.00),
(8, 'B', 10, 'VIP', 40000.00),
(8, 'C', 1, 'VIP', 40000.00), (8, 'C', 2, 'VIP', 40000.00), (8, 'C', 3, 'VIP', 40000.00),
(8, 'C', 4, 'VIP', 40000.00), (8, 'C', 5, 'VIP', 40000.00), (8, 'C', 6, 'VIP', 40000.00),
(8, 'C', 7, 'VIP', 40000.00), (8, 'C', 8, 'VIP', 40000.00), (8, 'C', 9, 'VIP', 40000.00),
(8, 'C', 10, 'VIP', 40000.00),
(8, 'D', 1, 'VIP', 40000.00), (8, 'D', 2, 'VIP', 40000.00), (8, 'D', 3, 'VIP', 40000.00),
(8, 'D', 4, 'VIP', 40000.00), (8, 'D', 5, 'VIP', 40000.00), (8, 'D', 6, 'VIP', 40000.00),
(8, 'D', 7, 'VIP', 40000.00), (8, 'D', 8, 'VIP', 40000.00), (8, 'D', 9, 'VIP', 40000.00),
(8, 'D', 10, 'VIP', 40000.00),
-- Row E (Couple - 10 seats)
(8, 'E', 1, 'COUPLE', 60000.00), (8, 'E', 2, 'COUPLE', 60000.00), (8, 'E', 3, 'COUPLE', 60000.00),
(8, 'E', 4, 'COUPLE', 60000.00), (8, 'E', 5, 'COUPLE', 60000.00), (8, 'E', 6, 'COUPLE', 60000.00),
(8, 'E', 7, 'COUPLE', 60000.00), (8, 'E', 8, 'COUPLE', 60000.00), (8, 'E', 9, 'COUPLE', 60000.00),
(8, 'E', 10, 'COUPLE', 60000.00),
-- Rows F-H (VIP - 30 seats total)
(8, 'F', 1, 'VIP', 40000.00), (8, 'F', 2, 'VIP', 40000.00), (8, 'F', 3, 'VIP', 40000.00),
(8, 'F', 4, 'VIP', 40000.00), (8, 'F', 5, 'VIP', 40000.00), (8, 'F', 6, 'VIP', 40000.00),
(8, 'F', 7, 'VIP', 40000.00), (8, 'F', 8, 'VIP', 40000.00), (8, 'F', 9, 'VIP', 40000.00),
(8, 'F', 10, 'VIP', 40000.00),
(8, 'G', 1, 'VIP', 40000.00), (8, 'G', 2, 'VIP', 40000.00), (8, 'G', 3, 'VIP', 40000.00),
(8, 'G', 4, 'VIP', 40000.00), (8, 'G', 5, 'VIP', 40000.00), (8, 'G', 6, 'VIP', 40000.00),
(8, 'G', 7, 'VIP', 40000.00), (8, 'G', 8, 'VIP', 40000.00), (8, 'G', 9, 'VIP', 40000.00),
(8, 'G', 10, 'VIP', 40000.00),
(8, 'H', 1, 'VIP', 40000.00), (8, 'H', 2, 'VIP', 40000.00), (8, 'H', 3, 'VIP', 40000.00),
(8, 'H', 4, 'VIP', 40000.00), (8, 'H', 5, 'VIP', 40000.00), (8, 'H', 6, 'VIP', 40000.00),
(8, 'H', 7, 'VIP', 40000.00), (8, 'H', 8, 'VIP', 40000.00), (8, 'H', 9, 'VIP', 40000.00),
(8, 'H', 10, 'VIP', 40000.00);

-- Insert seats for Room 9 (room_id=9, 70 seats, Standard)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Rows A-B (Regular - 20 seats total)
(9, 'A', 1, 'REGULAR', 0.00), (9, 'A', 2, 'REGULAR', 0.00), (9, 'A', 3, 'REGULAR', 0.00),
(9, 'A', 4, 'REGULAR', 0.00), (9, 'A', 5, 'REGULAR', 0.00), (9, 'A', 6, 'REGULAR', 0.00),
(9, 'A', 7, 'REGULAR', 0.00), (9, 'A', 8, 'REGULAR', 0.00), (9, 'A', 9, 'REGULAR', 0.00),
(9, 'A', 10, 'REGULAR', 0.00),
(9, 'B', 1, 'REGULAR', 0.00), (9, 'B', 2, 'REGULAR', 0.00), (9, 'B', 3, 'REGULAR', 0.00),
(9, 'B', 4, 'REGULAR', 0.00), (9, 'B', 5, 'REGULAR', 0.00), (9, 'B', 6, 'REGULAR', 0.00),
(9, 'B', 7, 'REGULAR', 0.00), (9, 'B', 8, 'REGULAR', 0.00), (9, 'B', 9, 'REGULAR', 0.00),
(9, 'B', 10, 'REGULAR', 0.00),
-- Rows C-D (VIP - 20 seats total)
(9, 'C', 1, 'VIP', 20000.00), (9, 'C', 2, 'VIP', 20000.00), (9, 'C', 3, 'VIP', 20000.00),
(9, 'C', 4, 'VIP', 20000.00), (9, 'C', 5, 'VIP', 20000.00), (9, 'C', 6, 'VIP', 20000.00),
(9, 'C', 7, 'VIP', 20000.00), (9, 'C', 8, 'VIP', 20000.00), (9, 'C', 9, 'VIP', 20000.00),
(9, 'C', 10, 'VIP', 20000.00),
(9, 'D', 1, 'VIP', 20000.00), (9, 'D', 2, 'VIP', 20000.00), (9, 'D', 3, 'VIP', 20000.00),
(9, 'D', 4, 'VIP', 20000.00), (9, 'D', 5, 'VIP', 20000.00), (9, 'D', 6, 'VIP', 20000.00),
(9, 'D', 7, 'VIP', 20000.00), (9, 'D', 8, 'VIP', 20000.00), (9, 'D', 9, 'VIP', 20000.00),
(9, 'D', 10, 'VIP', 20000.00),
-- Row E (Couple - 10 seats)
(9, 'E', 1, 'COUPLE', 30000.00), (9, 'E', 2, 'COUPLE', 30000.00), (9, 'E', 3, 'COUPLE', 30000.00),
(9, 'E', 4, 'COUPLE', 30000.00), (9, 'E', 5, 'COUPLE', 30000.00), (9, 'E', 6, 'COUPLE', 30000.00),
(9, 'E', 7, 'COUPLE', 30000.00), (9, 'E', 8, 'COUPLE', 30000.00), (9, 'E', 9, 'COUPLE', 30000.00),
(9, 'E', 10, 'COUPLE', 30000.00),
-- Rows F-G (Regular - 20 seats total)
(9, 'F', 1, 'REGULAR', 0.00), (9, 'F', 2, 'REGULAR', 0.00), (9, 'F', 3, 'REGULAR', 0.00),
(9, 'F', 4, 'REGULAR', 0.00), (9, 'F', 5, 'REGULAR', 0.00), (9, 'F', 6, 'REGULAR', 0.00),
(9, 'F', 7, 'REGULAR', 0.00), (9, 'F', 8, 'REGULAR', 0.00), (9, 'F', 9, 'REGULAR', 0.00),
(9, 'F', 10, 'REGULAR', 0.00),
(9, 'G', 1, 'REGULAR', 0.00), (9, 'G', 2, 'REGULAR', 0.00), (9, 'G', 3, 'REGULAR', 0.00),
(9, 'G', 4, 'REGULAR', 0.00), (9, 'G', 5, 'REGULAR', 0.00), (9, 'G', 6, 'REGULAR', 0.00),
(9, 'G', 7, 'REGULAR', 0.00), (9, 'G', 8, 'REGULAR', 0.00), (9, 'G', 9, 'REGULAR', 0.00),
(9, 'G', 10, 'REGULAR', 0.00);

-- Insert seats for Room 10 (room_id=10, 50 seats, Standard)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Row A (Regular - 10 seats)
(10, 'A', 1, 'REGULAR', 0.00), (10, 'A', 2, 'REGULAR', 0.00), (10, 'A', 3, 'REGULAR', 0.00),
(10, 'A', 4, 'REGULAR', 0.00), (10, 'A', 5, 'REGULAR', 0.00), (10, 'A', 6, 'REGULAR', 0.00),
(10, 'A', 7, 'REGULAR', 0.00), (10, 'A', 8, 'REGULAR', 0.00), (10, 'A', 9, 'REGULAR', 0.00),
(10, 'A', 10, 'REGULAR', 0.00),
-- Row B (VIP - 10 seats)
(10, 'B', 1, 'VIP', 20000.00), (10, 'B', 2, 'VIP', 20000.00), (10, 'B', 3, 'VIP', 20000.00),
(10, 'B', 4, 'VIP', 20000.00), (10, 'B', 5, 'VIP', 20000.00), (10, 'B', 6, 'VIP', 20000.00),
(10, 'B', 7, 'VIP', 20000.00), (10, 'B', 8, 'VIP', 20000.00), (10, 'B', 9, 'VIP', 20000.00),
(10, 'B', 10, 'VIP', 20000.00),
-- Row C (VIP - 10 seats)
(10, 'C', 1, 'VIP', 20000.00), (10, 'C', 2, 'VIP', 20000.00), (10, 'C', 3, 'VIP', 20000.00),
(10, 'C', 4, 'VIP', 20000.00), (10, 'C', 5, 'VIP', 20000.00), (10, 'C', 6, 'VIP', 20000.00),
(10, 'C', 7, 'VIP', 20000.00), (10, 'C', 8, 'VIP', 20000.00), (10, 'C', 9, 'VIP', 20000.00),
(10, 'C', 10, 'VIP', 20000.00),
-- Row D (Regular - 10 seats)
(10, 'D', 1, 'REGULAR', 0.00), (10, 'D', 2, 'REGULAR', 0.00), (10, 'D', 3, 'REGULAR', 0.00),
(10, 'D', 4, 'REGULAR', 0.00), (10, 'D', 5, 'REGULAR', 0.00), (10, 'D', 6, 'REGULAR', 0.00),
(10, 'D', 7, 'REGULAR', 0.00), (10, 'D', 8, 'REGULAR', 0.00), (10, 'D', 9, 'REGULAR', 0.00),
(10, 'D', 10, 'REGULAR', 0.00),
-- Row E (Regular - 10 seats)
(10, 'E', 1, 'REGULAR', 0.00), (10, 'E', 2, 'REGULAR', 0.00), (10, 'E', 3, 'REGULAR', 0.00),
(10, 'E', 4, 'REGULAR', 0.00), (10, 'E', 5, 'REGULAR', 0.00), (10, 'E', 6, 'REGULAR', 0.00),
(10, 'E', 7, 'REGULAR', 0.00), (10, 'E', 8, 'REGULAR', 0.00), (10, 'E', 9, 'REGULAR', 0.00),
(10, 'E', 10, 'REGULAR', 0.00);

-- Insert seats for Room 11 (room_id=11, 60 seats, Standard)
INSERT INTO seats (room_id, row_label, seat_number, seat_type, surcharge) VALUES
-- Row A (Regular - 10 seats)
(11, 'A', 1, 'REGULAR', 0.00), (11, 'A', 2, 'REGULAR', 0.00), (11, 'A', 3, 'REGULAR', 0.00),
(11, 'A', 4, 'REGULAR', 0.00), (11, 'A', 5, 'REGULAR', 0.00), (11, 'A', 6, 'REGULAR', 0.00),
(11, 'A', 7, 'REGULAR', 0.00), (11, 'A', 8, 'REGULAR', 0.00), (11, 'A', 9, 'REGULAR', 0.00),
(11, 'A', 10, 'REGULAR', 0.00),
-- Row B (VIP - 10 seats)
(11, 'B', 1, 'VIP', 20000.00), (11, 'B', 2, 'VIP', 20000.00), (11, 'B', 3, 'VIP', 20000.00),
(11, 'B', 4, 'VIP', 20000.00), (11, 'B', 5, 'VIP', 20000.00), (11, 'B', 6, 'VIP', 20000.00),
(11, 'B', 7, 'VIP', 20000.00), (11, 'B', 8, 'VIP', 20000.00), (11, 'B', 9, 'VIP', 20000.00),
(11, 'B', 10, 'VIP', 20000.00),
-- Row C (VIP - 10 seats)
(11, 'C', 1, 'VIP', 20000.00), (11, 'C', 2, 'VIP', 20000.00), (11, 'C', 3, 'VIP', 20000.00),
(11, 'C', 4, 'VIP', 20000.00), (11, 'C', 5, 'VIP', 20000.00), (11, 'C', 6, 'VIP', 20000.00),
(11, 'C', 7, 'VIP', 20000.00), (11, 'C', 8, 'VIP', 20000.00), (11, 'C', 9, 'VIP', 20000.00),
(11, 'C', 10, 'VIP', 20000.00),
-- Row D (Regular - 10 seats)
(11, 'D', 1, 'REGULAR', 0.00), (11, 'D', 2, 'REGULAR', 0.00), (11, 'D', 3, 'REGULAR', 0.00),
(11, 'D', 4, 'REGULAR', 0.00), (11, 'D', 5, 'REGULAR', 0.00), (11, 'D', 6, 'REGULAR', 0.00),
(11, 'D', 7, 'REGULAR', 0.00), (11, 'D', 8, 'REGULAR', 0.00), (11, 'D', 9, 'REGULAR', 0.00),
(11, 'D', 10, 'REGULAR', 0.00),
-- Row E (Regular - 10 seats)
(11, 'E', 1, 'REGULAR', 0.00), (11, 'E', 2, 'REGULAR', 0.00), (11, 'E', 3, 'REGULAR', 0.00),
(11, 'E', 4, 'REGULAR', 0.00), (11, 'E', 5, 'REGULAR', 0.00), (11, 'E', 6, 'REGULAR', 0.00),
(11, 'E', 7, 'REGULAR', 0.00), (11, 'E', 8, 'REGULAR', 0.00), (11, 'E', 9, 'REGULAR', 0.00),
(11, 'E', 10, 'REGULAR', 0.00),
-- Row F (Regular - 10 seats)
(11, 'F', 1, 'REGULAR', 0.00), (11, 'F', 2, 'REGULAR', 0.00), (11, 'F', 3, 'REGULAR', 0.00),
(11, 'F', 4, 'REGULAR', 0.00), (11, 'F', 5, 'REGULAR', 0.00), (11, 'F', 6, 'REGULAR', 0.00),
(11, 'F', 7, 'REGULAR', 0.00), (11, 'F', 8, 'REGULAR', 0.00), (11, 'F', 9, 'REGULAR', 0.00),
(11, 'F', 10, 'REGULAR', 0.00);

-- Insert sample showtimes
INSERT INTO showtimes (movie_id, room_id, cinema_id, start_time, end_time, base_price, status) VALUES
-- ============================================
-- August 1, 2026 (Today)
-- ============================================
-- Movie 1 (The Shawshank Redemption - 142 minutes)
(1, 1, 1, '2026-08-01 09:00:00', '2026-08-01 11:22:00', 80000.00, 'ACTIVE'),  -- CGV Vincom Center, Room 1, Morning
(1, 1, 1, '2026-08-01 14:00:00', '2026-08-01 16:22:00', 100000.00, 'ACTIVE'), -- CGV Vincom Center, Room 1, Afternoon
(1, 1, 1, '2026-08-01 18:00:00', '2026-08-01 20:22:00', 110000.00, 'ACTIVE'), -- CGV Vincom Center, Room 1, Evening
(1, 2, 1, '2026-08-01 20:00:00', '2026-08-01 22:22:00', 110000.00, 'ACTIVE'), -- CGV Vincom Center, Room 2, Night
(1, 4, 2, '2026-08-01 10:30:00', '2026-08-01 12:52:00', 85000.00, 'ACTIVE'),  -- CGV Aeon Mall, Room A
(1, 7, 3, '2026-08-01 16:30:00', '2026-08-01 18:52:00', 105000.00, 'ACTIVE'), -- Lotte Diamond Plaza, Standard
(1, 9, 5, '2026-08-01 13:00:00', '2026-08-01 15:22:00', 95000.00, 'ACTIVE'),  -- Galaxy Nguyen Du, Room 1
(1, 11, 6, '2026-08-01 19:00:00', '2026-08-01 21:22:00', 100000.00, 'ACTIVE'),-- Galaxy Tan Binh, Room A

-- Movie 2 (The Godfather - 175 minutes)
(2, 3, 1, '2026-08-01 10:00:00', '2026-08-01 12:55:00', 150000.00, 'ACTIVE'), -- CGV Vincom Center, IMAX
(2, 3, 1, '2026-08-01 15:00:00', '2026-08-01 17:55:00', 160000.00, 'ACTIVE'), -- CGV Vincom Center, IMAX
(2, 3, 1, '2026-08-01 19:30:00', '2026-08-01 22:25:00', 170000.00, 'ACTIVE'), -- CGV Vincom Center, IMAX, Prime
(2, 5, 2, '2026-08-01 17:00:00', '2026-08-01 19:55:00', 180000.00, 'ACTIVE'), -- CGV Aeon Mall, VIP Room
(2, 8, 4, '2026-08-01 14:30:00', '2026-08-01 17:25:00', 200000.00, 'ACTIVE'), -- Lotte Landmark 81, Premium
(2, 8, 4, '2026-08-01 20:00:00', '2026-08-01 22:55:00', 210000.00, 'ACTIVE'), -- Lotte Landmark 81, Premium Night

-- Movie 3 (The Dark Knight - 152 minutes)
(3, 6, 3, '2026-08-01 11:00:00', '2026-08-01 13:32:00', 140000.00, 'ACTIVE'), -- Lotte Diamond Plaza, 4DX
(3, 6, 3, '2026-08-01 16:00:00', '2026-08-01 18:32:00', 150000.00, 'ACTIVE'), -- Lotte Diamond Plaza, 4DX
(3, 6, 3, '2026-08-01 21:00:00', '2026-08-01 23:32:00', 160000.00, 'ACTIVE'), -- Lotte Diamond Plaza, 4DX
(3, 9, 5, '2026-08-01 17:00:00', '2026-08-01 19:32:00', 110000.00, 'ACTIVE'), -- Galaxy Nguyen Du, Room 1
(3, 10, 5, '2026-08-01 20:30:00', '2026-08-01 23:02:00', 115000.00, 'ACTIVE'),-- Galaxy Nguyen Du, Room 2

-- ============================================
-- August 2, 2026 (Tomorrow)
-- ============================================
-- Movie 1 (142 minutes)
(1, 1, 1, '2026-08-02 09:00:00', '2026-08-02 11:22:00', 80000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-02 14:00:00', '2026-08-02 16:22:00', 100000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-02 18:00:00', '2026-08-02 20:22:00', 110000.00, 'ACTIVE'),
(1, 2, 1, '2026-08-02 12:00:00', '2026-08-02 14:22:00', 95000.00, 'ACTIVE'),
(1, 2, 1, '2026-08-02 20:00:00', '2026-08-02 22:22:00', 110000.00, 'ACTIVE'),
(1, 4, 2, '2026-08-02 10:30:00', '2026-08-02 12:52:00', 85000.00, 'ACTIVE'),
(1, 4, 2, '2026-08-02 15:30:00', '2026-08-02 17:52:00', 100000.00, 'ACTIVE'),
(1, 7, 3, '2026-08-02 16:30:00', '2026-08-02 18:52:00', 105000.00, 'ACTIVE'),
(1, 9, 5, '2026-08-02 13:00:00', '2026-08-02 15:22:00', 95000.00, 'ACTIVE'),
(1, 11, 6, '2026-08-02 19:00:00', '2026-08-02 21:22:00', 100000.00, 'ACTIVE'),

-- Movie 2 (175 minutes)
(2, 3, 1, '2026-08-02 10:00:00', '2026-08-02 12:55:00', 150000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-02 15:00:00', '2026-08-02 17:55:00', 160000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-02 19:30:00', '2026-08-02 22:25:00', 170000.00, 'ACTIVE'),
(2, 5, 2, '2026-08-02 13:00:00', '2026-08-02 15:55:00', 175000.00, 'ACTIVE'),
(2, 5, 2, '2026-08-02 17:00:00', '2026-08-02 19:55:00', 180000.00, 'ACTIVE'),
(2, 8, 4, '2026-08-02 14:30:00', '2026-08-02 17:25:00', 200000.00, 'ACTIVE'),
(2, 8, 4, '2026-08-02 20:00:00', '2026-08-02 22:55:00', 210000.00, 'ACTIVE'),

-- Movie 3 (152 minutes)
(3, 6, 3, '2026-08-02 11:00:00', '2026-08-02 13:32:00', 140000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-02 16:00:00', '2026-08-02 18:32:00', 150000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-02 21:00:00', '2026-08-02 23:32:00', 160000.00, 'ACTIVE'),
(3, 9, 5, '2026-08-02 17:00:00', '2026-08-02 19:32:00', 110000.00, 'ACTIVE'),
(3, 10, 5, '2026-08-02 20:30:00', '2026-08-02 23:02:00', 115000.00, 'ACTIVE'),
(3, 11, 6, '2026-08-02 14:00:00', '2026-08-02 16:32:00', 105000.00, 'ACTIVE'),

-- ============================================
-- August 3, 2026 (Weekend Saturday)
-- ============================================
-- Movie 1 (142 minutes)
(1, 1, 1, '2026-08-03 09:00:00', '2026-08-03 11:22:00', 90000.00, 'ACTIVE'),  -- Weekend morning
(1, 1, 1, '2026-08-03 11:30:00', '2026-08-03 13:52:00', 110000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-03 14:00:00', '2026-08-03 16:22:00', 120000.00, 'ACTIVE'), -- Weekend pricing
(1, 1, 1, '2026-08-03 18:00:00', '2026-08-03 20:22:00', 130000.00, 'ACTIVE'),
(1, 2, 1, '2026-08-03 12:00:00', '2026-08-03 14:22:00', 115000.00, 'ACTIVE'),
(1, 2, 1, '2026-08-03 20:00:00', '2026-08-03 22:22:00', 130000.00, 'ACTIVE'),
(1, 4, 2, '2026-08-03 10:00:00', '2026-08-03 12:22:00', 95000.00, 'ACTIVE'),
(1, 4, 2, '2026-08-03 15:00:00', '2026-08-03 17:22:00', 110000.00, 'ACTIVE'),
(1, 7, 3, '2026-08-03 13:00:00', '2026-08-03 15:22:00', 115000.00, 'ACTIVE'),
(1, 7, 3, '2026-08-03 19:00:00', '2026-08-03 21:22:00', 125000.00, 'ACTIVE'),

-- Movie 2 (175 minutes)
(2, 3, 1, '2026-08-03 10:00:00', '2026-08-03 12:55:00', 170000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-03 13:30:00', '2026-08-03 16:25:00', 180000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-03 17:00:00', '2026-08-03 19:55:00', 190000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-03 20:30:00', '2026-08-03 23:25:00', 200000.00, 'ACTIVE'),
(2, 5, 2, '2026-08-03 12:00:00', '2026-08-03 14:55:00', 195000.00, 'ACTIVE'),
(2, 5, 2, '2026-08-03 18:00:00', '2026-08-03 20:55:00', 210000.00, 'ACTIVE'),
(2, 8, 4, '2026-08-03 11:00:00', '2026-08-03 13:55:00', 220000.00, 'ACTIVE'),
(2, 8, 4, '2026-08-03 15:00:00', '2026-08-03 17:55:00', 230000.00, 'ACTIVE'),
(2, 8, 4, '2026-08-03 21:00:00', '2026-08-03 23:55:00', 240000.00, 'ACTIVE'),

-- Movie 3 (152 minutes)
(3, 6, 3, '2026-08-03 10:00:00', '2026-08-03 12:32:00', 160000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-03 14:00:00', '2026-08-03 16:32:00', 170000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-03 18:00:00', '2026-08-03 20:32:00', 180000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-03 22:00:00', '2026-08-04 00:32:00', 190000.00, 'ACTIVE'), -- Late night weekend
(3, 9, 5, '2026-08-03 12:00:00', '2026-08-03 14:32:00', 120000.00, 'ACTIVE'),
(3, 9, 5, '2026-08-03 16:00:00', '2026-08-03 18:32:00', 130000.00, 'ACTIVE'),
(3, 10, 5, '2026-08-03 19:00:00', '2026-08-03 21:32:00', 135000.00, 'ACTIVE'),

-- ============================================
-- August 4, 2026 (Weekend Sunday)
-- ============================================
-- Movie 1 (142 minutes)
(1, 1, 1, '2026-08-04 09:00:00', '2026-08-04 11:22:00', 90000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-04 11:30:00', '2026-08-04 13:52:00', 110000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-04 14:00:00', '2026-08-04 16:22:00', 120000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-04 18:00:00', '2026-08-04 20:22:00', 130000.00, 'ACTIVE'),
(1, 2, 1, '2026-08-04 16:00:00', '2026-08-04 18:22:00', 120000.00, 'ACTIVE'),
(1, 2, 1, '2026-08-04 20:00:00', '2026-08-04 22:22:00', 130000.00, 'ACTIVE'),
(1, 9, 5, '2026-08-04 10:00:00', '2026-08-04 12:22:00', 100000.00, 'ACTIVE'),
(1, 9, 5, '2026-08-04 15:00:00', '2026-08-04 17:22:00', 115000.00, 'ACTIVE'),
(1, 11, 6, '2026-08-04 13:00:00', '2026-08-04 15:22:00', 110000.00, 'ACTIVE'),

-- Movie 2 (175 minutes)
(2, 3, 1, '2026-08-04 10:00:00', '2026-08-04 12:55:00', 170000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-04 13:30:00', '2026-08-04 16:25:00', 180000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-04 17:00:00', '2026-08-04 19:55:00', 190000.00, 'ACTIVE'),
(2, 5, 2, '2026-08-04 14:00:00', '2026-08-04 16:55:00', 195000.00, 'ACTIVE'),
(2, 5, 2, '2026-08-04 19:00:00', '2026-08-04 21:55:00', 210000.00, 'ACTIVE'),
(2, 8, 4, '2026-08-04 11:00:00', '2026-08-04 13:55:00', 220000.00, 'ACTIVE'),
(2, 8, 4, '2026-08-04 16:00:00', '2026-08-04 18:55:00', 230000.00, 'ACTIVE'),

-- Movie 3 (152 minutes)
(3, 6, 3, '2026-08-04 10:00:00', '2026-08-04 12:32:00', 160000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-04 14:00:00', '2026-08-04 16:32:00', 170000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-04 18:00:00', '2026-08-04 20:32:00', 180000.00, 'ACTIVE'),
(3, 9, 5, '2026-08-04 17:00:00', '2026-08-04 19:32:00', 130000.00, 'ACTIVE'),
(3, 10, 5, '2026-08-04 20:00:00', '2026-08-04 22:32:00', 135000.00, 'ACTIVE'),

-- ============================================
-- August 5-7, 2026 (Next Weekdays)
-- ============================================
-- Movie 1 at various times
(1, 1, 1, '2026-08-05 14:00:00', '2026-08-05 16:22:00', 100000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-05 18:00:00', '2026-08-05 20:22:00', 110000.00, 'ACTIVE'),
(1, 9, 5, '2026-08-05 13:00:00', '2026-08-05 15:22:00', 95000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-06 14:00:00', '2026-08-06 16:22:00', 100000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-06 18:00:00', '2026-08-06 20:22:00', 110000.00, 'ACTIVE'),
(1, 9, 5, '2026-08-06 13:00:00', '2026-08-06 15:22:00', 95000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-07 14:00:00', '2026-08-07 16:22:00', 100000.00, 'ACTIVE'),
(1, 1, 1, '2026-08-07 18:00:00', '2026-08-07 20:22:00', 110000.00, 'ACTIVE'),

-- Movie 2 at various times
(2, 3, 1, '2026-08-05 15:00:00', '2026-08-05 17:55:00', 160000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-05 19:30:00', '2026-08-05 22:25:00', 170000.00, 'ACTIVE'),
(2, 8, 4, '2026-08-05 20:00:00', '2026-08-05 22:55:00', 210000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-06 15:00:00', '2026-08-06 17:55:00', 160000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-06 19:30:00', '2026-08-06 22:25:00', 170000.00, 'ACTIVE'),
(2, 8, 4, '2026-08-06 20:00:00', '2026-08-06 22:55:00', 210000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-07 15:00:00', '2026-08-07 17:55:00', 160000.00, 'ACTIVE'),
(2, 3, 1, '2026-08-07 19:30:00', '2026-08-07 22:25:00', 170000.00, 'ACTIVE'),

-- Movie 3 at various times
(3, 6, 3, '2026-08-05 16:00:00', '2026-08-05 18:32:00', 150000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-05 21:00:00', '2026-08-05 23:32:00', 160000.00, 'ACTIVE'),
(3, 9, 5, '2026-08-05 17:00:00', '2026-08-05 19:32:00', 110000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-06 16:00:00', '2026-08-06 18:32:00', 150000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-06 21:00:00', '2026-08-06 23:32:00', 160000.00, 'ACTIVE'),
(3, 9, 5, '2026-08-06 17:00:00', '2026-08-06 19:32:00', 110000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-07 16:00:00', '2026-08-07 18:32:00', 150000.00, 'ACTIVE'),
(3, 6, 3, '2026-08-07 21:00:00', '2026-08-07 23:32:00', 160000.00, 'ACTIVE');

-- ============================================
-- End of initialization script
-- ============================================