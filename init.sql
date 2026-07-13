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
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS users;

-- ============================================
-- Table: users
-- Description: User account information
-- ============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
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
    release_date DATE,
    genre VARCHAR(255),
    director VARCHAR(255),
    cast TEXT,
    rating VARCHAR(10),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: showtimes
-- Description: Movie screening schedules
-- ============================================
CREATE TABLE showtimes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    showtime DATETIME NOT NULL,
    room VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    available_seats INT NOT NULL DEFAULT 0,
    total_seats INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: seats
-- Description: Seat inventory
-- ============================================
CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_code VARCHAR(10) NOT NULL UNIQUE COMMENT 'e.g., A1, A2, B1',
    seat_type ENUM('NORMAL', 'VIP', 'COUPLE') NOT NULL DEFAULT 'NORMAL',
    status ENUM('AVAILABLE', 'SOLD') NOT NULL DEFAULT 'AVAILABLE',
    row_number VARCHAR(5) NOT NULL,
    seat_number INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
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
    status ENUM('PENDING', 'CONFIRMED', 'PAID', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    booking_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_time DATETIME NULL COMMENT 'Time when booking expires if not paid',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: booking_details
-- Description: Seats in a booking order
-- ============================================
CREATE TABLE booking_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE,
    UNIQUE KEY uk_booking_seat (booking_id, seat_id)
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
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Sample Data (Optional)
-- ============================================

-- Insert sample users
INSERT INTO users (name, email, password, role) VALUES
('Admin User', 'admin@example.com', '$2a$10$dummyHashedPassword1', 'ADMIN'),
('John Doe', 'john.doe@example.com', '$2a$10$dummyHashedPassword2', 'USER'),
('Jane Smith', 'jane.smith@example.com', '$2a$10$dummyHashedPassword3', 'USER');

-- Insert sample movies
INSERT INTO movies (title, duration, description, image_url, release_date, genre, rating) VALUES
('The Shawshank Redemption', 142, 'Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.', 'https://example.com/shawshank.jpg', '1994-09-23', 'Drama', 'R'),
('The Godfather', 175, 'The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.', 'https://example.com/godfather.jpg', '1972-03-24', 'Crime, Drama', 'R'),
('The Dark Knight', 152, 'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests.', 'https://example.com/darkknight.jpg', '2008-07-18', 'Action, Crime, Drama', 'PG-13');

-- Insert sample showtimes
INSERT INTO showtimes (movie_id, showtime, room, price, available_seats, total_seats) VALUES
(1, '2026-07-10 14:00:00', 'Room A', 100000.00, 50, 50),
(1, '2026-07-10 18:00:00', 'Room A', 100000.00, 50, 50),
(2, '2026-07-10 15:00:00', 'Room B', 120000.00, 60, 60),
(3, '2026-07-10 19:00:00', 'Room C', 150000.00, 80, 80);

-- Insert sample seats (for Room A)
INSERT INTO seats (seat_code, seat_type, status, row_number, seat_number) VALUES
('A1', 'NORMAL', 'AVAILABLE', 'A', 1),
('A2', 'NORMAL', 'AVAILABLE', 'A', 2),
('A3', 'NORMAL', 'AVAILABLE', 'A', 3),
('A4', 'NORMAL', 'AVAILABLE', 'A', 4),
('A5', 'NORMAL', 'AVAILABLE', 'A', 5),
('B1', 'VIP', 'AVAILABLE', 'B', 1),
('B2', 'VIP', 'AVAILABLE', 'B', 2),
('B3', 'VIP', 'AVAILABLE', 'B', 3),
('C1', 'COUPLE', 'AVAILABLE', 'C', 1),
('C2', 'COUPLE', 'AVAILABLE', 'C', 2);

-- ============================================
-- End of initialization script
-- ============================================