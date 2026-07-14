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
    cinema_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    base_price DECIMAL(10,2) NOT NULL COMMENT 'Base ticket price',
    status ENUM('ACTIVE', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    FOREIGN KEY (cinema_id) REFERENCES cinemas(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    INDEX idx_movie_id (movie_id),
    INDEX idx_cinema_id (cinema_id),
    INDEX idx_room_id (room_id),
    INDEX idx_start_time (start_time),
    INDEX idx_movie_cinema (movie_id, cinema_id)
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
    expired_at DATETIME NULL,
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
    payment_time DATETIME NULL,
    response_data TEXT COMMENT 'JSON response from payment gateway',
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

-- Insert sample showtimes
INSERT INTO showtimes (movie_id, cinema_id, room_id, start_time, end_time, base_price, status) VALUES
-- Movie 1 (142 minutes) at CGV Vincom Center (cinema_id=1)
(1, 1, 1, '2026-07-15 14:00:00', '2026-07-15 16:22:00', 100000.00, 'ACTIVE'),
(1, 1, 1, '2026-07-15 18:00:00', '2026-07-15 20:22:00', 100000.00, 'ACTIVE'),
(1, 1, 2, '2026-07-15 20:00:00', '2026-07-15 22:22:00', 100000.00, 'ACTIVE'),
-- Movie 2 (175 minutes) at CGV Vincom Center (cinema_id=1, IMAX Room)
(2, 1, 3, '2026-07-15 15:00:00', '2026-07-15 17:55:00', 150000.00, 'ACTIVE'),
(2, 1, 3, '2026-07-15 19:30:00', '2026-07-15 22:25:00', 150000.00, 'ACTIVE'),
-- Movie 3 (152 minutes) at Lotte Cinema Diamond Plaza (cinema_id=3, 4DX Room)
(3, 3, 6, '2026-07-15 16:00:00', '2026-07-15 18:32:00', 120000.00, 'ACTIVE'),
(3, 3, 6, '2026-07-15 21:00:00', '2026-07-15 23:32:00', 120000.00, 'ACTIVE'),
-- Movie 1 at Lotte Cinema Landmark 81 (cinema_id=4, Premium Room)
(1, 4, 8, '2026-07-16 14:30:00', '2026-07-16 16:52:00', 180000.00, 'ACTIVE'),
-- Movie 3 at Galaxy Nguyen Du (cinema_id=5)
(3, 5, 9, '2026-07-16 17:00:00', '2026-07-16 19:32:00', 110000.00, 'ACTIVE');

-- ============================================
-- End of initialization script
-- ============================================