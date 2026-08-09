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
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
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
    FOREIGN KEY (cinema_id) REFERENCES cinemas(id) ON DELETE CASCADE
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
    UNIQUE KEY uk_room_seat (room_id, row_label, seat_number)
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
    FOREIGN KEY (cinema_id) REFERENCES cinemas(id) ON DELETE CASCADE
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
    price DECIMAL(10,2) NOT NULL COMMENT 'Price snapshot at booking time',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
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
    paid_at DATETIME NULL,
    payment_url VARCHAR(2000),
    gateway_response TEXT COMMENT 'JSON response from payment gateway',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Sample Data
-- ============================================

-- Insert sample users
INSERT INTO users (username, email, password, full_name, phone_number, role) VALUES
('admin', 'admin@example.com', '$2a$10$dummyHashedPassword1', 'Admin User', '0901234567', 'ROLE_ADMIN'),
('johndoe', 'john.doe@example.com', '$2a$10$dummyHashedPassword2', 'John Doe', '0912345678', 'ROLE_USER'),
('janesmith', 'jane.smith@example.com', '$2a$10$dummyHashedPassword3', 'Jane Smith', '0923456789', 'ROLE_USER');

-- Insert sample movies (Currently showing)
INSERT INTO movies (title, duration, description, image_url, release_date, genre, rating) VALUES
('The Shawshank Redemption', 142, 'Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.', 'http://localhost:4566/ticket-online-media/shawshank.jpg', '1994-09-23', 'Drama', 'C16'),
('The Godfather', 175, 'The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.', 'http://localhost:4566/ticket-online-media/godfather.jpg', '1972-03-24', 'Crime, Drama', 'C18'),
('The Dark Knight', 152, 'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests.', 'http://localhost:4566/ticket-online-media/darkknight.jpg', '2008-07-18', 'Action, Crime, Drama', 'C13');

-- Insert upcoming movies (Coming soon - release dates after August 8, 2026)
INSERT INTO movies (title, duration, description, image_url, trailer_url, release_date, genre, director, cast, rating) VALUES
-- August 2026 releases
('Mission: Impossible 8', 163, 'Ethan Hunt and his team embark on their most dangerous mission yet to track down a terrifying new weapon.', 'http://localhost:4566/ticket-online-media/mi8.jpg', 'https://youtube.com/mi8', '2026-08-15', 'Action, Adventure, Thriller', 'Christopher McQuarrie', 'Tom Cruise, Hayley Atwell, Ving Rhames', 'C13'),
('The Marvels 2', 125, 'Carol Danvers teams up with Kamala Khan and Monica Rambeau to face a new cosmic threat.', 'http://localhost:4566/ticket-online-media/marvels2.jpg', 'https://youtube.com/marvels2', '2026-08-22', 'Action, Adventure, Fantasy', 'Nia DaCosta', 'Brie Larson, Iman Vellani, Teyonah Parris', 'C13'),
('Wicked', 140, 'The untold story of the witches of Oz, exploring the friendship between Elphaba and Glinda.', 'http://localhost:4566/ticket-online-media/wicked.jpg', 'https://youtube.com/wicked', '2026-08-29', 'Fantasy, Musical, Romance', 'Jon M. Chu', 'Cynthia Erivo, Ariana Grande, Michelle Yeoh', 'P'),

-- September 2026 releases
('Gladiator 2', 155, 'Years after witnessing the death of Maximus, Lucius must enter the Colosseum after his home is conquered.', 'http://localhost:4566/ticket-online-media/gladiator2.jpg', 'https://youtube.com/gladiator2', '2026-09-05', 'Action, Adventure, Drama', 'Ridley Scott', 'Paul Mescal, Denzel Washington, Pedro Pascal', 'C16'),
('Fantastic Four', 135, 'Marvel Studios introduces the First Family of the Marvel Universe.', 'http://localhost:4566/ticket-online-media/fantastic4.jpg', 'https://youtube.com/fantastic4', '2026-09-12', 'Action, Adventure, Sci-Fi', 'Matt Shakman', 'Pedro Pascal, Vanessa Kirby, Joseph Quinn', 'C13'),
('Joker: Folie à Deux', 138, 'Arthur Fleck is institutionalized at Arkham where he meets the love of his life, Harley Quinn.', 'http://localhost:4566/ticket-online-media/joker2.jpg', 'https://youtube.com/joker2', '2026-09-19', 'Crime, Drama, Musical', 'Todd Phillips', 'Joaquin Phoenix, Lady Gaga, Brendan Gleeson', 'C18'),
('The Hunger Games: Sunrise on the Reaping', 145, 'The story of Haymitch Abernathy and his victory in the 50th Hunger Games.', 'http://localhost:4566/ticket-online-media/hungergames.jpg', 'https://youtube.com/hungergames', '2026-09-26', 'Action, Adventure, Sci-Fi', 'Francis Lawrence', 'TBA', 'C13'),

-- October 2026 releases
('Beetlejuice 3', 120, 'The ghost with the most returns for another supernatural adventure.', 'http://localhost:4566/ticket-online-media/beetlejuice3.jpg', 'https://youtube.com/beetlejuice3', '2026-10-03', 'Comedy, Fantasy, Horror', 'Tim Burton', 'Michael Keaton, Winona Ryder, Jenna Ortega', 'C13'),
('Venom 3', 115, 'Eddie Brock and Venom face their most dangerous threat yet.', 'http://localhost:4566/ticket-online-media/venom3.jpg', 'https://youtube.com/venom3', '2026-10-10', 'Action, Sci-Fi, Thriller', 'Kelly Marcel', 'Tom Hardy, Chiwetel Ejiofor, Juno Temple', 'C16'),
('Nosferatu', 132, 'A gothic tale of obsession between a haunted young woman and the vampire infatuated with her.', 'http://localhost:4566/ticket-online-media/nosferatu.jpg', 'https://youtube.com/nosferatu', '2026-10-17', 'Horror, Mystery', 'Robert Eggers', 'Bill Skarsgård, Lily-Rose Depp, Nicholas Hoult', 'C18'),
('Sonic the Hedgehog 3', 109, 'Sonic, Tails, and Knuckles face a powerful new adversary, Shadow the Hedgehog.', 'http://localhost:4566/ticket-online-media/sonic3.jpg', 'https://youtube.com/sonic3', '2026-10-24', 'Action, Adventure, Comedy', 'Jeff Fowler', 'Ben Schwartz, Jim Carrey, Keanu Reeves', 'P'),
('Smile 2', 127, 'A pop star begins experiencing terrifying and inexplicable events before her world tour.', 'http://localhost:4566/ticket-online-media/smile2.jpg', 'https://youtube.com/smile2', '2026-10-31', 'Horror, Mystery, Thriller', 'Parker Finn', 'Naomi Scott, Rosemarie DeWitt, Kyle Gallner', 'C18'),

-- November 2026 releases
('Moana 2', 100, 'Moana embarks on a new voyage across the ocean to connect the people of all islands.', 'http://localhost:4566/ticket-online-media/moana2.jpg', 'https://youtube.com/moana2', '2026-11-07', 'Animation, Adventure, Comedy', 'David Derrick Jr.', 'Auli\'i Cravalho, Dwayne Johnson', 'P'),
('Kraven the Hunter', 127, 'The origin story of one of Spider-Man\'s most iconic villains.', 'http://localhost:4566/ticket-online-media/kraven.jpg', 'https://youtube.com/kraven', '2026-11-14', 'Action, Adventure, Thriller', 'J.C. Chandor', 'Aaron Taylor-Johnson, Russell Crowe, Ariana DeBose', 'C16'),
('Wicked: Part Two', 145, 'The conclusion of the Wicked saga as Elphaba and Glinda\'s paths diverge.', 'http://localhost:4566/ticket-online-media/wicked2.jpg', 'https://youtube.com/wicked2', '2026-11-21', 'Fantasy, Musical, Romance', 'Jon M. Chu', 'Cynthia Erivo, Ariana Grande, Michelle Yeoh', 'P'),
('Avatar 3', 195, 'Jake Sully and Neytiri\'s family continues to explore Pandora\'s diverse regions and cultures.', 'http://localhost:4566/ticket-online-media/avatar3.jpg', 'https://youtube.com/avatar3', '2026-11-28', 'Action, Adventure, Fantasy', 'James Cameron', 'Sam Worthington, Zoe Saldana, Kate Winslet', 'C13'),

-- December 2026 releases
('Mufasa: The Lion King', 118, 'The origin story of Mufasa and his rise to become the king of the Pride Lands.', 'http://localhost:4566/ticket-online-media/mufasa.jpg', 'https://youtube.com/mufasa', '2026-12-05', 'Animation, Adventure, Drama', 'Barry Jenkins', 'Aaron Pierre, Kelvin Harrison Jr., Donald Glover', 'P'),
('Blade', 133, 'The legendary vampire hunter returns to protect humanity from the undead.', 'http://localhost:4566/ticket-online-media/blade.jpg', 'https://youtube.com/blade', '2026-12-12', 'Action, Horror, Sci-Fi', 'Yann Demange', 'Mahershala Ali, Delroy Lindo', 'C18'),
('Captain America: Brave New World', 142, 'Sam Wilson officially takes on the mantle of Captain America and uncovers a global conspiracy.', 'http://localhost:4566/ticket-online-media/captainamerica.jpg', 'https://youtube.com/captainamerica', '2026-12-19', 'Action, Adventure, Sci-Fi', 'Julius Onah', 'Anthony Mackie, Harrison Ford, Danny Ramirez', 'C13'),
('Untitled Star Wars Film', 155, 'A new chapter in the Star Wars saga exploring unknown regions of the galaxy.', 'http://localhost:4566/ticket-online-media/starwars.jpg', 'https://youtube.com/starwars', '2026-12-26', 'Action, Adventure, Fantasy', 'Shawn Levy', 'Daisy Ridley, Oscar Isaac', 'C13');

-- Insert sample cinemas
INSERT INTO cinemas (brand, name, logo_url, address, district, city, phone, website, description) VALUES
-- CGV Cinemas
('CGV', 'CGV Vincom Center', 'http://localhost:4566/ticket-online-media/cgv-logo.png', '72 Le Thanh Ton, District 1', 'District 1', 'Ho Chi Minh City', '1900-6017', 'https://cgv.vn', 'Leading cinema chain in Vietnam'),
('CGV', 'CGV Aeon Mall', 'http://localhost:4566/ticket-online-media/cgv-logo.png', '30 Bo Bao Tan Thang, Son Ky', 'Tan Phu District', 'Ho Chi Minh City', '1900-6017', 'https://cgv.vn', 'Leading cinema chain in Vietnam'),
-- Lotte Cinema
('Lotte Cinema', 'Lotte Cinema Diamond Plaza', 'http://localhost:4566/ticket-online-media/lotte-logo.png', '34 Le Duan, District 1', 'District 1', 'Ho Chi Minh City', '1900-6520', 'https://lottecinema.com.vn', 'Premium cinema experience'),
('Lotte Cinema', 'Lotte Cinema Landmark 81', 'http://localhost:4566/ticket-online-media/lotte-logo.png', '208 Nguyen Huu Canh, Ward 22', 'Binh Thanh District', 'Ho Chi Minh City', '1900-6520', 'https://lottecinema.com.vn', 'Premium cinema experience'),
-- Galaxy Cinema
('Galaxy Cinema', 'Galaxy Nguyen Du', 'http://localhost:4566/ticket-online-media/galaxy-logo.png', '116 Nguyen Du, District 1', 'District 1', 'Ho Chi Minh City', '1900-2224', 'https://galaxycine.vn', 'Modern cinema with latest technology'),
('Galaxy Cinema', 'Galaxy Tan Binh', 'http://localhost:4566/ticket-online-media/galaxy-logo.png', '246 Nguyen Hong Dao, Ward 13', 'Tan Binh District', 'Ho Chi Minh City', '1900-2224', 'https://galaxycine.vn', 'Modern cinema with latest technology');

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
-- PRODUCTION-LIKE SAMPLE DATA
-- ============================================

-- ============================================
-- Additional Users (50 more users for realistic data)
-- ============================================
INSERT INTO users (username, email, password, full_name, phone_number, role, created_at) VALUES
('nguyenvana', 'nguyen.van.a@gmail.com', '$2a$10$dummyHashedPassword4', 'Nguyễn Văn A', '0901234501', 'ROLE_USER', '2026-01-15 10:30:00'),
('tranthib', 'tran.thi.b@gmail.com', '$2a$10$dummyHashedPassword5', 'Trần Thị B', '0901234502', 'ROLE_USER', '2026-01-20 14:20:00'),
('levanc', 'le.van.c@gmail.com', '$2a$10$dummyHashedPassword6', 'Lê Văn C', '0901234503', 'ROLE_USER', '2026-02-01 09:15:00'),
('phamthid', 'pham.thi.d@gmail.com', '$2a$10$dummyHashedPassword7', 'Phạm Thị D', '0901234504', 'ROLE_USER', '2026-02-10 16:45:00'),
('hoangvane', 'hoang.van.e@gmail.com', '$2a$10$dummyHashedPassword8', 'Hoàng Văn E', '0901234505', 'ROLE_USER', '2026-02-15 11:30:00'),
('vuthif', 'vu.thi.f@gmail.com', '$2a$10$dummyHashedPassword9', 'Vũ Thị F', '0901234506', 'ROLE_USER', '2026-03-01 13:20:00'),
('dovang', 'do.van.g@gmail.com', '$2a$10$dummyHashedPassword10', 'Đỗ Văn G', '0901234507', 'ROLE_USER', '2026-03-05 15:10:00'),
('dangthih', 'dang.thi.h@gmail.com', '$2a$10$dummyHashedPassword11', 'Đặng Thị H', '0901234508', 'ROLE_USER', '2026-03-10 08:25:00'),
('buivani', 'bui.van.i@gmail.com', '$2a$10$dummyHashedPassword12', 'Bùi Văn I', '0901234509', 'ROLE_USER', '2026-03-15 17:40:00'),
('duongthij', 'duong.thi.j@gmail.com', '$2a$10$dummyHashedPassword13', 'Dương Thị J', '0901234510', 'ROLE_USER', '2026-03-20 10:55:00'),
('lyvanк', 'ly.van.k@gmail.com', '$2a$10$dummyHashedPassword14', 'Lý Văn K', '0901234511', 'ROLE_USER', '2026-04-01 12:30:00'),
('ngothil', 'ngo.thi.l@gmail.com', '$2a$10$dummyHashedPassword15', 'Ngô Thị L', '0901234512', 'ROLE_USER', '2026-04-05 14:15:00'),
('dinhvanm', 'dinh.van.m@gmail.com', '$2a$10$dummyHashedPassword16', 'Đinh Văn M', '0901234513', 'ROLE_USER', '2026-04-10 09:45:00'),
('vothin', 'vo.thi.n@gmail.com', '$2a$10$dummyHashedPassword17', 'Võ Thị N', '0901234514', 'ROLE_USER', '2026-04-15 16:20:00'),
('truongvano', 'truong.van.o@gmail.com', '$2a$10$dummyHashedPassword18', 'Trương Văn O', '0901234515', 'ROLE_USER', '2026-04-20 11:10:00'),
('phanthip', 'phan.thi.p@gmail.com', '$2a$10$dummyHashedPassword19', 'Phan Thị P', '0901234516', 'ROLE_USER', '2026-05-01 13:35:00'),
('nguyenvanq', 'nguyen.van.q@gmail.com', '$2a$10$dummyHashedPassword20', 'Nguyễn Văn Q', '0901234517', 'ROLE_USER', '2026-05-05 15:50:00'),
('tranthir', 'tran.thi.r@gmail.com', '$2a$10$dummyHashedPassword21', 'Trần Thị R', '0901234518', 'ROLE_USER', '2026-05-10 10:25:00'),
('levans', 'le.van.s@gmail.com', '$2a$10$dummyHashedPassword22', 'Lê Văn S', '0901234519', 'ROLE_USER', '2026-05-15 14:40:00'),
('phamthit', 'pham.thi.t@gmail.com', '$2a$10$dummyHashedPassword23', 'Phạm Thị T', '0901234520', 'ROLE_USER', '2026-05-20 09:55:00'),
('hoangvanu', 'hoang.van.u@gmail.com', '$2a$10$dummyHashedPassword24', 'Hoàng Văn U', '0901234521', 'ROLE_USER', '2026-06-01 12:15:00'),
('vuthiv', 'vu.thi.v@gmail.com', '$2a$10$dummyHashedPassword25', 'Vũ Thị V', '0901234522', 'ROLE_USER', '2026-06-05 16:30:00'),
('dovanw', 'do.van.w@gmail.com', '$2a$10$dummyHashedPassword26', 'Đỗ Văn W', '0901234523', 'ROLE_USER', '2026-06-10 11:45:00'),
('dangthix', 'dang.thi.x@gmail.com', '$2a$10$dummyHashedPassword27', 'Đặng Thị X', '0901234524', 'ROLE_USER', '2026-06-15 13:20:00'),
('buivany', 'bui.van.y@gmail.com', '$2a$10$dummyHashedPassword28', 'Bùi Văn Y', '0901234525', 'ROLE_USER', '2026-06-20 15:35:00'),
('duongthiz', 'duong.thi.z@gmail.com', '$2a$10$dummyHashedPassword29', 'Dương Thị Z', '0901234526', 'ROLE_USER', '2026-06-25 10:50:00'),
('nguyenminh', 'nguyen.minh@gmail.com', '$2a$10$dummyHashedPassword30', 'Nguyễn Minh', '0901234527', 'ROLE_USER', '2026-07-01 14:05:00'),
('trananh', 'tran.anh@gmail.com', '$2a$10$dummyHashedPassword31', 'Trần Anh', '0901234528', 'ROLE_USER', '2026-07-05 09:20:00'),
('letuan', 'le.tuan@gmail.com', '$2a$10$dummyHashedPassword32', 'Lê Tuấn', '0901234529', 'ROLE_USER', '2026-07-10 16:40:00'),
('phamlinh', 'pham.linh@gmail.com', '$2a$10$dummyHashedPassword33', 'Phạm Linh', '0901234530', 'ROLE_USER', '2026-07-15 11:55:00'),
('hoangdung', 'hoang.dung@gmail.com', '$2a$10$dummyHashedPassword34', 'Hoàng Dung', '0901234531', 'ROLE_USER', '2026-07-20 13:10:00'),
('vuhung', 'vu.hung@gmail.com', '$2a$10$dummyHashedPassword35', 'Vũ Hùng', '0901234532', 'ROLE_USER', '2026-07-25 15:25:00'),
('doquan', 'do.quan@gmail.com', '$2a$10$dummyHashedPassword36', 'Đỗ Quân', '0901234533', 'ROLE_USER', '2026-07-28 10:40:00'),
('danghieu', 'dang.hieu@gmail.com', '$2a$10$dummyHashedPassword37', 'Đặng Hiếu', '0901234534', 'ROLE_USER', '2026-07-30 14:55:00'),
('buinam', 'bui.nam@gmail.com', '$2a$10$dummyHashedPassword38', 'Bùi Nam', '0901234535', 'ROLE_USER', '2026-08-01 09:10:00'),
('duongthao', 'duong.thao@gmail.com', '$2a$10$dummyHashedPassword39', 'Dương Thảo', '0901234536', 'ROLE_USER', '2026-08-02 12:25:00'),
('lyhang', 'ly.hang@gmail.com', '$2a$10$dummyHashedPassword40', 'Lý Hằng', '0901234537', 'ROLE_USER', '2026-08-03 16:40:00'),
('ngophuong', 'ngo.phuong@gmail.com', '$2a$10$dummyHashedPassword41', 'Ngô Phương', '0901234538', 'ROLE_USER', '2026-08-04 11:55:00'),
('dinhson', 'dinh.son@gmail.com', '$2a$10$dummyHashedPassword42', 'Đinh Sơn', '0901234539', 'ROLE_USER', '2026-08-05 13:10:00'),
('vomai', 'vo.mai@gmail.com', '$2a$10$dummyHashedPassword43', 'Võ Mai', '0901234540', 'ROLE_USER', '2026-08-06 15:25:00'),
('truongkhanh', 'truong.khanh@gmail.com', '$2a$10$dummyHashedPassword44', 'Trương Khánh', '0901234541', 'ROLE_USER', '2026-08-07 10:40:00'),
('phanhoa', 'phan.hoa@gmail.com', '$2a$10$dummyHashedPassword45', 'Phan Hoa', '0901234542', 'ROLE_USER', '2026-08-07 14:55:00'),
('nguyenlong', 'nguyen.long@gmail.com', '$2a$10$dummyHashedPassword46', 'Nguyễn Long', '0912345671', 'ROLE_USER', '2026-08-07 16:10:00'),
('tranbao', 'tran.bao@gmail.com', '$2a$10$dummyHashedPassword47', 'Trần Bảo', '0912345672', 'ROLE_USER', '2026-08-07 17:25:00'),
('lehieu', 'le.hieu@gmail.com', '$2a$10$dummyHashedPassword48', 'Lê Hiếu', '0912345673', 'ROLE_USER', '2026-08-07 18:40:00'),
('phamvy', 'pham.vy@gmail.com', '$2a$10$dummyHashedPassword49', 'Phạm Vy', '0912345674', 'ROLE_USER', '2026-08-07 19:55:00'),
('hoangtam', 'hoang.tam@gmail.com', '$2a$10$dummyHashedPassword50', 'Hoàng Tâm', '0912345675', 'ROLE_USER', '2026-08-07 20:10:00'),
('vukhang', 'vu.khang@gmail.com', '$2a$10$dummyHashedPassword51', 'Vũ Khang', '0912345676', 'ROLE_USER', '2026-08-09 08:25:00'),
('dothien', 'do.thien@gmail.com', '$2a$10$dummyHashedPassword52', 'Đỗ Thiên', '0912345677', 'ROLE_USER', '2026-08-09 09:40:00'),
('danghuyen', 'dang.huyen@gmail.com', '$2a$10$dummyHashedPassword53', 'Đặng Huyền', '0912345678', 'ROLE_USER', '2026-08-09 10:55:00'),
('buiphat', 'bui.phat@gmail.com', '$2a$10$dummyHashedPassword54', 'Bùi Phát', '0912345679', 'ROLE_USER', '2026-08-09 12:10:00');

-- ============================================
-- Additional Movies (22 more movies)
-- ============================================
INSERT INTO movies (title, duration, description, image_url, trailer_url, release_date, genre, director, cast, rating) VALUES
('Oppenheimer', 180, 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.', 'http://localhost:4566/ticket-online-media/oppenheimer.jpg', 'https://youtube.com/oppenheimer', '2023-07-21', 'Biography, Drama, History', 'Christopher Nolan', 'Cillian Murphy, Emily Blunt, Matt Damon', 'C16'),
('Barbie', 114, 'Barbie suffers a crisis that leads her to question her world and her existence.', 'http://localhost:4566/ticket-online-media/barbie.jpg', 'https://youtube.com/barbie', '2023-07-21', 'Adventure, Comedy, Fantasy', 'Greta Gerwig', 'Margot Robbie, Ryan Gosling, Issa Rae', 'C13'),
('Dune: Part Two', 166, 'Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.', 'http://localhost:4566/ticket-online-media/dune2.jpg', 'https://youtube.com/dune2', '2024-03-01', 'Action, Adventure, Drama', 'Denis Villeneuve', 'Timothée Chalamet, Zendaya, Rebecca Ferguson', 'C13'),
('Deadpool & Wolverine', 128, 'Deadpool teams up with Wolverine for an epic adventure.', 'http://localhost:4566/ticket-online-media/deadpool3.jpg', 'https://youtube.com/deadpool3', '2024-07-26', 'Action, Comedy, Sci-Fi', 'Shawn Levy', 'Ryan Reynolds, Hugh Jackman, Emma Corrin', 'C18'),
('Inside Out 2', 96, 'Riley enters puberty and experiences brand new emotions.', 'http://localhost:4566/ticket-online-media/insideout2.jpg', 'https://youtube.com/insideout2', '2024-06-14', 'Animation, Adventure, Comedy', 'Kelsey Mann', 'Amy Poehler, Maya Hawke, Kensington Tallman', 'P'),
('The Batman', 176, 'Batman ventures into Gotham City underworld when a sadistic killer leaves behind a trail of cryptic clues.', 'http://localhost:4566/ticket-online-media/batman.jpg', 'https://youtube.com/batman', '2022-03-04', 'Action, Crime, Drama', 'Matt Reeves', 'Robert Pattinson, Zoë Kravitz, Jeffrey Wright', 'C16'),
('Avatar: The Way of Water', 192, 'Jake Sully lives with his newfound family formed on the extrasolar moon Pandora.', 'http://localhost:4566/ticket-online-media/avatar2.jpg', 'https://youtube.com/avatar2', '2022-12-16', 'Action, Adventure, Fantasy', 'James Cameron', 'Sam Worthington, Zoe Saldana, Sigourney Weaver', 'C13'),
('Top Gun: Maverick', 130, 'After thirty years, Maverick is still pushing the envelope as a top naval aviator.', 'http://localhost:4566/ticket-online-media/topgun.jpg', 'https://youtube.com/topgun', '2022-05-27', 'Action, Drama', 'Joseph Kosinski', 'Tom Cruise, Jennifer Connelly, Miles Teller', 'C13'),
('Spider-Man: No Way Home', 148, 'Spider-Man seeks Doctor Strange help, but when a spell goes wrong, dangerous foes from other worlds start to appear.', 'http://localhost:4566/ticket-online-media/spiderman.jpg', 'https://youtube.com/spiderman', '2021-12-17', 'Action, Adventure, Fantasy', 'Jon Watts', 'Tom Holland, Zendaya, Benedict Cumberbatch', 'C13'),
('Avengers: Endgame', 181, 'After the devastating events, the Avengers assemble once more to reverse Thanos actions.', 'http://localhost:4566/ticket-online-media/endgame.jpg', 'https://youtube.com/endgame', '2019-04-26', 'Action, Adventure, Drama', 'Anthony Russo, Joe Russo', 'Robert Downey Jr., Chris Evans, Mark Ruffalo', 'C13'),
('Inception', 148, 'A thief who steals corporate secrets through the use of dream-sharing technology.', 'http://localhost:4566/ticket-online-media/inception.jpg', 'https://youtube.com/inception', '2010-07-16', 'Action, Sci-Fi, Thriller', 'Christopher Nolan', 'Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page', 'C13'),
('The Matrix', 136, 'A computer hacker learns about the true nature of reality and his role in the war against its controllers.', 'http://localhost:4566/ticket-online-media/matrix.jpg', 'https://youtube.com/matrix', '1999-03-31', 'Action, Sci-Fi', 'Lana Wachowski, Lilly Wachowski', 'Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss', 'C16'),
('Interstellar', 169, 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity survival.', 'http://localhost:4566/ticket-online-media/interstellar.jpg', 'https://youtube.com/interstellar', '2014-11-07', 'Adventure, Drama, Sci-Fi', 'Christopher Nolan', 'Matthew McConaughey, Anne Hathaway, Jessica Chastain', 'C13'),
('Parasite', 132, 'Greed and class discrimination threaten the newly formed symbiotic relationship between two families.', 'http://localhost:4566/ticket-online-media/parasite.jpg', 'https://youtube.com/parasite', '2019-05-30', 'Drama, Thriller', 'Bong Joon Ho', 'Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong', 'C16'),
('Joker', 122, 'A mentally troubled comedian is disregarded and mistreated by society, igniting a downward spiral.', 'http://localhost:4566/ticket-online-media/joker.jpg', 'https://youtube.com/joker', '2019-10-04', 'Crime, Drama, Thriller', 'Todd Phillips', 'Joaquin Phoenix, Robert De Niro, Zazie Beetz', 'C18'),
('The Lord of the Rings: The Return of the King', 201, 'Gandalf and Aragorn lead the World of Men against Saurons army to draw his gaze from Frodo and Sam.', 'http://localhost:4566/ticket-online-media/lotr3.jpg', 'https://youtube.com/lotr3', '2003-12-17', 'Action, Adventure, Drama', 'Peter Jackson', 'Elijah Wood, Viggo Mortensen, Ian McKellen', 'C13'),
('Pulp Fiction', 154, 'The lives of two mob hitmen, a boxer, a gangster and his wife intertwine in four tales of violence.', 'http://localhost:4566/ticket-online-media/pulp.jpg', 'https://youtube.com/pulp', '1994-10-14', 'Crime, Drama', 'Quentin Tarantino', 'John Travolta, Uma Thurman, Samuel L. Jackson', 'C18'),
('Forrest Gump', 142, 'The presidencies of Kennedy and Johnson unfold through the perspective of an Alabama man.', 'http://localhost:4566/ticket-online-media/forrest.jpg', 'https://youtube.com/forrest', '1994-07-06', 'Drama, Romance', 'Robert Zemeckis', 'Tom Hanks, Robin Wright, Gary Sinise', 'C13'),
('The Green Mile', 189, 'The lives of guards on Death Row are affected by one of their charges: a black man accused of murder.', 'http://localhost:4566/ticket-online-media/greenmile.jpg', 'https://youtube.com/greenmile', '1999-12-10', 'Crime, Drama, Fantasy', 'Frank Darabont', 'Tom Hanks, Michael Clarke Duncan, David Morse', 'C16'),
('Gladiator', 155, 'A former Roman General sets out to exact vengeance against the corrupt emperor who murdered his family.', 'http://localhost:4566/ticket-online-media/gladiator.jpg', 'https://youtube.com/gladiator', '2000-05-05', 'Action, Adventure, Drama', 'Ridley Scott', 'Russell Crowe, Joaquin Phoenix, Connie Nielsen', 'C16'),
('The Silence of the Lambs', 118, 'A young FBI cadet must receive the help of an incarcerated cannibal killer to catch another serial killer.', 'http://localhost:4566/ticket-online-media/silence.jpg', 'https://youtube.com/silence', '1991-02-14', 'Crime, Drama, Thriller', 'Jonathan Demme', 'Jodie Foster, Anthony Hopkins, Lawrence A. Bonney', 'C18'),
('Saving Private Ryan', 169, 'Following the Normandy Landings, a group of soldiers go behind enemy lines to retrieve a paratrooper.', 'http://localhost:4566/ticket-online-media/ryan.jpg', 'https://youtube.com/ryan', '1998-07-24', 'Drama, War', 'Steven Spielberg', 'Tom Hanks, Matt Damon, Tom Sizemore', 'C16');

-- ============================================
-- Additional Cinemas (9 more cinemas)
-- ============================================
INSERT INTO cinemas (brand, name, logo_url, address, district, city, phone, website, description) VALUES
-- More CGV locations
('CGV', 'CGV Vincom Mega Mall', 'http://localhost:4566/ticket-online-media/cgv-logo.png', '159 Xa Lo Ha Noi, Thu Duc City', 'Thu Duc City', 'Ho Chi Minh City', '1900-6017', 'https://cgv.vn', 'Large cinema complex in Thu Duc'),
('CGV', 'CGV Crescent Mall', 'http://localhost:4566/ticket-online-media/cgv-logo.png', '101 Ton Dat Tien, District 7', 'District 7', 'Ho Chi Minh City', '1900-6017', 'https://cgv.vn', 'Premium cinema in District 7'),
('CGV', 'CGV Su Van Hanh', 'http://localhost:4566/ticket-online-media/cgv-logo.png', '54A Su Van Hanh, District 10', 'District 10', 'Ho Chi Minh City', '1900-6017', 'https://cgv.vn', 'Convenient location in District 10'),
-- More Lotte Cinema locations
('Lotte Cinema', 'Lotte Cinema Cong Hoa', 'http://localhost:4566/ticket-online-media/lotte-logo.png', '829 Cong Hoa, Tan Binh District', 'Tan Binh District', 'Ho Chi Minh City', '1900-6520', 'https://lottecinema.com.vn', 'Modern cinema in Tan Binh'),
('Lotte Cinema', 'Lotte Cinema Phu Tho', 'http://localhost:4566/ticket-online-media/lotte-logo.png', '1A Cach Mang Thang Tam, District 11', 'District 11', 'Ho Chi Minh City', '1900-6520', 'https://lottecinema.com.vn', 'Cinema near Phu Tho stadium'),
-- More Galaxy Cinema locations
('Galaxy Cinema', 'Galaxy Kinh Duong Vuong', 'http://localhost:4566/ticket-online-media/galaxy-logo.png', '718B Kinh Duong Vuong, Binh Tan District', 'Binh Tan District', 'Ho Chi Minh City', '1900-2224', 'https://galaxycine.vn', 'Modern cinema in Binh Tan'),
('Galaxy Cinema', 'Galaxy Quang Trung', 'http://localhost:4566/ticket-online-media/galaxy-logo.png', '190 Quang Trung, Go Vap District', 'Go Vap District', 'Ho Chi Minh City', '1900-2224', 'https://galaxycine.vn', 'Popular cinema in Go Vap'),
-- BHD Star Cineplex
('BHD Star Cineplex', 'BHD Star Bitexco', 'http://localhost:4566/ticket-online-media/bhd-logo.png', '2 Hai Trieu, District 1', 'District 1', 'Ho Chi Minh City', '1900-2099', 'https://bhdstar.vn', 'Premium cinema in Bitexco Tower'),
('BHD Star Cineplex', 'BHD Star Vincom 3/2', 'http://localhost:4566/ticket-online-media/bhd-logo.png', '3/2 Street, District 10', 'District 10', 'Ho Chi Minh City', '1900-2099', 'https://bhdstar.vn', 'Cinema in Vincom 3/2');

-- ============================================
-- Additional Rooms (35 more rooms for new cinemas)
-- ============================================
INSERT INTO rooms (cinema_id, name, capacity, room_type) VALUES
-- CGV Vincom Mega Mall (cinema_id=7)
(7, 'Room 1', 80, 'Standard'),
(7, 'Room 2', 75, 'Standard'),
(7, 'Room 3', 70, 'Standard'),
(7, 'IMAX Room', 150, 'IMAX'),
-- CGV Crescent Mall (cinema_id=8)
(8, 'Room 1', 65, 'Standard'),
(8, 'Room 2', 60, 'Standard'),
(8, 'Gold Class', 40, 'VIP'),
-- CGV Su Van Hanh (cinema_id=9)
(9, 'Room 1', 55, 'Standard'),
(9, 'Room 2', 50, 'Standard'),
(9, 'Room 3', 45, 'Standard'),
-- Lotte Cinema Cong Hoa (cinema_id=10)
(10, 'Standard 1', 70, 'Standard'),
(10, 'Standard 2', 65, 'Standard'),
(10, 'Premium', 50, 'VIP'),
-- Lotte Cinema Phu Tho (cinema_id=11)
(11, 'Room A', 60, 'Standard'),
(11, 'Room B', 55, 'Standard'),
(11, '4DX', 45, '4DX'),
-- Galaxy Kinh Duong Vuong (cinema_id=12)
(12, 'Room 1', 75, 'Standard'),
(12, 'Room 2', 70, 'Standard'),
(12, 'VIP Room', 35, 'VIP'),
-- Galaxy Quang Trung (cinema_id=13)
(13, 'Room 1', 65, 'Standard'),
(13, 'Room 2', 60, 'Standard'),
(13, 'Room 3', 55, 'Standard'),
-- BHD Star Bitexco (cinema_id=14)
(14, 'Gold 1', 50, 'VIP'),
(14, 'Gold 2', 45, 'VIP'),
(14, 'IMAX', 120, 'IMAX'),
-- BHD Star Vincom 3/2 (cinema_id=15)
(15, 'Room 1', 60, 'Standard'),
(15, 'Room 2', 55, 'Standard'),
(15, 'Premium', 40, 'VIP');

-- ============================================
-- Additional Showtimes for new movies (200+ more showtimes)
-- ============================================
INSERT INTO showtimes (movie_id, room_id, cinema_id, start_time, end_time, base_price, status) VALUES
-- August 8, 2026 - Oppenheimer (movie_id=4)
(4, 3, 1, '2026-08-09 10:00:00', '2026-08-09 13:00:00', 170000.00, 'ACTIVE'),
(4, 3, 1, '2026-08-09 14:00:00', '2026-08-09 17:00:00', 180000.00, 'ACTIVE'),
(4, 3, 1, '2026-08-09 19:00:00', '2026-08-09 22:00:00', 190000.00, 'ACTIVE'),
(4, 15, 7, '2026-08-09 11:00:00', '2026-08-09 14:00:00', 175000.00, 'ACTIVE'),
(4, 15, 7, '2026-08-09 18:00:00', '2026-08-09 21:00:00', 185000.00, 'ACTIVE'),
(4, 27, 14, '2026-08-09 20:00:00', '2026-08-09 23:00:00', 220000.00, 'ACTIVE'),

-- Barbie (movie_id=5)
(5, 1, 1, '2026-08-09 10:30:00', '2026-08-09 12:24:00', 90000.00, 'ACTIVE'),
(5, 1, 1, '2026-08-09 13:00:00', '2026-08-09 14:54:00', 95000.00, 'ACTIVE'),
(5, 1, 1, '2026-08-09 16:00:00', '2026-08-09 17:54:00', 100000.00, 'ACTIVE'),
(5, 2, 1, '2026-08-09 18:30:00', '2026-08-09 20:24:00', 105000.00, 'ACTIVE'),
(5, 12, 7, '2026-08-09 14:00:00', '2026-08-09 15:54:00', 95000.00, 'ACTIVE'),
(5, 19, 8, '2026-08-09 17:00:00', '2026-08-09 18:54:00', 110000.00, 'ACTIVE'),

-- Dune: Part Two (movie_id=6)
(6, 3, 1, '2026-08-09 13:30:00', '2026-08-09 16:16:00', 180000.00, 'ACTIVE'),
(6, 15, 7, '2026-08-09 15:00:00', '2026-08-09 17:46:00', 185000.00, 'ACTIVE'),
(6, 27, 14, '2026-08-09 16:00:00', '2026-08-09 18:46:00', 220000.00, 'ACTIVE'),

-- Inside Out 2 (movie_id=8)
(8, 2, 1, '2026-08-09 11:00:00', '2026-08-09 12:36:00', 85000.00, 'ACTIVE'),
(8, 2, 1, '2026-08-09 14:00:00', '2026-08-09 15:36:00', 90000.00, 'ACTIVE'),
(8, 12, 7, '2026-08-09 10:00:00', '2026-08-09 11:36:00', 85000.00, 'ACTIVE'),
(8, 12, 7, '2026-08-09 16:00:00', '2026-08-09 17:36:00', 95000.00, 'ACTIVE'),

-- Spider-Man: No Way Home (movie_id=12)
(12, 1, 1, '2026-08-09 20:00:00', '2026-08-09 22:28:00', 115000.00, 'ACTIVE'),
(12, 15, 7, '2026-08-09 19:30:00', '2026-08-09 21:58:00', 120000.00, 'ACTIVE'),
(12, 27, 14, '2026-08-09 21:00:00', '2026-08-09 23:28:00', 230000.00, 'ACTIVE');

-- ============================================
-- Sample Bookings (Historical booking data)
-- ============================================
INSERT INTO bookings (booking_code, user_id, showtime_id, total_amount, status, created_at, confirmed_at) VALUES
('BK20260801001', 2, 1, 160000.00, 'CONFIRMED', '2026-07-30 10:15:00', '2026-07-30 10:20:00'),
('BK20260801002', 3, 1, 240000.00, 'CONFIRMED', '2026-07-30 14:30:00', '2026-07-30 14:35:00'),
('BK20260801003', 4, 2, 300000.00, 'CONFIRMED', '2026-07-31 09:00:00', '2026-07-31 09:05:00'),
('BK20260801004', 5, 3, 220000.00, 'CONFIRMED', '2026-07-31 15:45:00', '2026-07-31 15:50:00'),
('BK20260801005', 6, 9, 450000.00, 'CONFIRMED', '2026-08-01 08:00:00', '2026-08-01 08:05:00'),
('BK20260802006', 7, 11, 340000.00, 'CONFIRMED', '2026-08-01 10:30:00', '2026-08-01 10:35:00'),
('BK20260802007', 8, 12, 320000.00, 'CONFIRMED', '2026-08-01 12:00:00', '2026-08-01 12:05:00'),
('BK20260802008', 9, 15, 420000.00, 'CONFIRMED', '2026-08-01 16:30:00', '2026-08-01 16:35:00'),
('BK20260803009', 10, 17, 280000.00, 'CONFIRMED', '2026-08-02 09:15:00', '2026-08-02 09:20:00'),
('BK20260803010', 11, 18, 310000.00, 'CONFIRMED', '2026-08-02 11:45:00', '2026-08-02 11:50:00'),
('BK20260803011', 12, 20, 290000.00, 'CONFIRMED', '2026-08-02 14:20:00', '2026-08-02 14:25:00'),
('BK20260803012', 13, 25, 260000.00, 'CONFIRMED', '2026-08-02 18:00:00', '2026-08-02 18:05:00'),
('BK20260804013', 14, 28, 380000.00, 'CONFIRMED', '2026-08-03 08:30:00', '2026-08-03 08:35:00'),
('BK20260804014', 15, 30, 240000.00, 'CONFIRMED', '2026-08-03 10:00:00', '2026-08-03 10:05:00'),
('BK20260804015', 16, 35, 360000.00, 'CONFIRMED', '2026-08-03 13:30:00', '2026-08-03 13:35:00'),
('BK20260804016', 17, 40, 320000.00, 'CONFIRMED', '2026-08-03 17:00:00', '2026-08-03 17:05:00'),
('BK20260805017', 18, 45, 340000.00, 'CONFIRMED', '2026-08-04 09:45:00', '2026-08-04 09:50:00'),
('BK20260805018', 19, 48, 280000.00, 'CONFIRMED', '2026-08-04 12:15:00', '2026-08-04 12:20:00'),
('BK20260805019', 20, 52, 460000.00, 'CONFIRMED', '2026-08-04 15:30:00', '2026-08-04 15:35:00'),
('BK20260805020', 21, 57, 300000.00, 'CONFIRMED', '2026-08-05 10:00:00', '2026-08-05 10:05:00'),
('BK20260806021', 22, 60, 220000.00, 'CONFIRMED', '2026-08-05 14:30:00', '2026-08-05 14:35:00'),
('BK20260806022', 23, 64, 380000.00, 'CONFIRMED', '2026-08-06 09:00:00', '2026-08-06 09:05:00'),
('BK20260806023', 24, 68, 240000.00, 'CONFIRMED', '2026-08-06 13:30:00', '2026-08-06 13:35:00'),
('BK20260807024', 25, 72, 340000.00, 'CONFIRMED', '2026-08-07 08:45:00', '2026-08-07 08:50:00'),
('BK20260807025', 26, 75, 320000.00, 'CONFIRMED', '2026-08-07 12:00:00', '2026-08-07 12:05:00'),
('BK20260807026', 27, 1, 160000.00, 'CANCELLED', '2026-07-29 15:00:00', NULL),
('BK20260807027', 28, 5, 200000.00, 'EXPIRED', '2026-07-30 20:00:00', NULL),
('BK20260808028', 29, 80, 350000.00, 'PENDING', '2026-08-09 14:00:00', NULL);

-- ============================================
-- Booking Details (seats in bookings)
-- ============================================
INSERT INTO booking_details (booking_id, seat_id, price) VALUES
-- Booking 1 (2 seats)
(1, 1, 80000.00),
(1, 2, 80000.00),
-- Booking 2 (3 seats)
(2, 3, 80000.00),
(2, 4, 80000.00),
(2, 5, 80000.00),
-- Booking 3 (3 VIP seats)
(3, 11, 120000.00),
(3, 12, 120000.00),
(3, 13, 120000.00),
-- Booking 4 (2 VIP seats)
(4, 14, 130000.00),
(4, 15, 130000.00),
-- Booking 5 (3 VIP seats in IMAX)
(5, 37, 180000.00),
(5, 38, 180000.00),
(5, 39, 180000.00),
-- Booking 6 (2 VIP seats)
(6, 40, 190000.00),
(6, 41, 190000.00),
-- Booking 7 (2 VIP seats)
(7, 42, 180000.00),
(7, 43, 180000.00),
-- Booking 8 (2 VIP seats)
(8, 44, 210000.00),
(8, 45, 210000.00),
-- Booking 9 (2 regular seats)
(9, 6, 150000.00),
(9, 7, 130000.00),
-- Booking 10 (2 VIP seats)
(10, 16, 160000.00),
(10, 17, 150000.00),
-- Booking 11 (2 regular seats)
(11, 8, 150000.00),
(11, 9, 140000.00),
-- Booking 12 (2 regular seats)
(12, 10, 130000.00),
(12, 25, 130000.00),
-- Booking 13 (2 VIP seats)
(13, 18, 190000.00),
(13, 19, 190000.00),
-- Booking 14 (2 regular seats)
(14, 26, 120000.00),
(14, 27, 120000.00),
-- Booking 15 (2 VIP seats)
(15, 46, 180000.00),
(15, 47, 180000.00),
-- Booking 16 (2 VIP seats)
(16, 48, 160000.00),
(16, 49, 160000.00),
-- Booking 17 (2 VIP seats)
(17, 20, 170000.00),
(17, 21, 170000.00),
-- Booking 18 (2 regular seats)
(18, 28, 140000.00),
(18, 29, 140000.00),
-- Booking 19 (2 VIP seats)
(19, 50, 230000.00),
(19, 51, 230000.00),
-- Booking 20 (2 VIP seats)
(20, 22, 150000.00),
(20, 23, 150000.00),
-- Booking 21 (2 regular seats)
(21, 30, 110000.00),
(21, 31, 110000.00),
-- Booking 22 (2 VIP seats)
(22, 52, 190000.00),
(22, 53, 190000.00),
-- Booking 23 (2 regular seats)
(23, 32, 120000.00),
(23, 33, 120000.00),
-- Booking 24 (2 VIP seats)
(24, 54, 170000.00),
(24, 55, 170000.00),
-- Booking 25 (2 VIP seats)
(25, 24, 160000.00),
(25, 56, 160000.00),
-- Booking 26 (2 cancelled)
(26, 34, 80000.00),
(26, 35, 80000.00),
-- Booking 27 (2 expired)
(27, 57, 100000.00),
(27, 58, 100000.00),
-- Booking 28 (2 pending)
(28, 59, 175000.00),
(28, 60, 175000.00);

-- ============================================
-- Payment Records
-- ============================================
INSERT INTO payments (booking_id, transaction_id, payment_method, amount, status, paid_at) VALUES
(1, 'VNP20260730101501', 'VNPAY', 160000.00, 'SUCCESS', '2026-07-30 10:20:00'),
(2, 'VNP20260730143002', 'VNPAY', 240000.00, 'SUCCESS', '2026-07-30 14:35:00'),
(3, 'MOMO20260731090003', 'MOMO', 300000.00, 'SUCCESS', '2026-07-31 09:05:00'),
(4, 'VNP20260731154504', 'VNPAY', 220000.00, 'SUCCESS', '2026-07-31 15:50:00'),
(5, 'VNP20260801080005', 'VNPAY', 450000.00, 'SUCCESS', '2026-08-01 08:05:00'),
(6, 'MOMO20260801103006', 'MOMO', 340000.00, 'SUCCESS', '2026-08-01 10:35:00'),
(7, 'VNP20260801120007', 'VNPAY', 320000.00, 'SUCCESS', '2026-08-01 12:05:00'),
(8, 'VNP20260801163008', 'VNPAY', 420000.00, 'SUCCESS', '2026-08-01 16:35:00'),
(9, 'MOMO20260802091509', 'MOMO', 280000.00, 'SUCCESS', '2026-08-02 09:20:00'),
(10, 'VNP20260802114510', 'VNPAY', 310000.00, 'SUCCESS', '2026-08-02 11:50:00'),
(11, 'VNP20260802142011', 'VNPAY', 290000.00, 'SUCCESS', '2026-08-02 14:25:00'),
(12, 'MOMO20260802180012', 'MOMO', 260000.00, 'SUCCESS', '2026-08-02 18:05:00'),
(13, 'VNP20260803083013', 'VNPAY', 380000.00, 'SUCCESS', '2026-08-03 08:35:00'),
(14, 'VNP20260803100014', 'VNPAY', 240000.00, 'SUCCESS', '2026-08-03 10:05:00'),
(15, 'MOMO20260803133015', 'MOMO', 360000.00, 'SUCCESS', '2026-08-03 13:35:00'),
(16, 'VNP20260803170016', 'VNPAY', 320000.00, 'SUCCESS', '2026-08-03 17:05:00'),
(17, 'VNP20260804094517', 'VNPAY', 340000.00, 'SUCCESS', '2026-08-04 09:50:00'),
(18, 'MOMO20260804121518', 'MOMO', 280000.00, 'SUCCESS', '2026-08-04 12:20:00'),
(19, 'VNP20260804153019', 'VNPAY', 460000.00, 'SUCCESS', '2026-08-04 15:35:00'),
(20, 'VNP20260805100020', 'VNPAY', 300000.00, 'SUCCESS', '2026-08-05 10:05:00'),
(21, 'MOMO20260805143021', 'MOMO', 220000.00, 'SUCCESS', '2026-08-05 14:35:00'),
(22, 'VNP20260806090022', 'VNPAY', 380000.00, 'SUCCESS', '2026-08-06 09:05:00'),
(23, 'VNP20260806133023', 'VNPAY', 240000.00, 'SUCCESS', '2026-08-06 13:35:00'),
(24, 'MOMO20260807084524', 'MOMO', 340000.00, 'SUCCESS', '2026-08-07 08:50:00'),
(25, 'VNP20260807120025', 'VNPAY', 320000.00, 'SUCCESS', '2026-08-07 12:05:00'),
(26, 'VNP20260729150026', 'VNPAY', 160000.00, 'REFUNDED', '2026-07-29 15:05:00'),
(27, 'VNP20260730200027', 'VNPAY', 200000.00, 'FAILED', NULL),
(28, 'VNP20260808140028', 'VNPAY', 350000.00, 'PENDING', NULL);

-- ============================================
-- End of initialization script
-- ============================================
