-- Test schema for ticket-online project
-- Compatible with H2 (MODE=MYSQL) and MySQL for quick testing

-- Seats table (matches com.ticket_online.domain.catalog.domain.Seat)
CREATE TABLE IF NOT EXISTS seats (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  show_id BIGINT,
  seat_code VARCHAR(255),
  status VARCHAR(32),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table (matches com.ticket_online.domain.booking.domain.Order)
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  show_id BIGINT,
  status VARCHAR(32),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- OrderSeat table (matches com.ticket_online.domain.booking.domain.OrderSeat)
CREATE TABLE IF NOT EXISTS order_seat (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT,
  seat_id BIGINT,
  CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_seat FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE SET NULL
);

-- Sample data for quick tests
-- Minimal seeds for show 1
-- Minimal seeds for show 1
INSERT INTO seats (show_id, seat_code, status) VALUES (1, 'A1', 'AVAILABLE');
INSERT INTO seats (show_id, seat_code, status) VALUES (1, 'A2', 'AVAILABLE');
INSERT INTO seats (show_id, seat_code, status) VALUES (1, 'A3', 'SOLD'); -- sold
INSERT INTO seats (show_id, seat_code, status) VALUES (1, 'B1', 'AVAILABLE');
INSERT INTO seats (show_id, seat_code, status) VALUES (1, 'B2', 'AVAILABLE');

-- Seeds for show 2
-- Seeds for show 2
INSERT INTO seats (show_id, seat_code, status) VALUES (2, 'A1', 'AVAILABLE');
INSERT INTO seats (show_id, seat_code, status) VALUES (2, 'A2', 'SOLD'); -- sold
INSERT INTO seats (show_id, seat_code, status) VALUES (2, 'A3', 'AVAILABLE');
INSERT INTO seats (show_id, seat_code, status) VALUES (2, 'B1', 'AVAILABLE');

-- Orders: mix of PENDING(0), PAID(1), CANCELLED(2)
-- Orders: mix of PENDING, PAID, CANCELLED
INSERT INTO orders (user_id, show_id, status) VALUES (100, 1, 'PENDING');
INSERT INTO orders (user_id, show_id, status) VALUES (101, 1, 'PAID');
INSERT INTO orders (user_id, show_id, status) VALUES (102, 2, 'CANCELLED');
INSERT INTO orders (user_id, show_id, status) VALUES (103, 2, 'PAID');

-- Map orders to seats
-- order 1 (pending) reserves seat 1 (A1)
-- Map orders to seats
-- order 1 (pending) reserves seat 1 (A1)
INSERT INTO order_seat (order_id, seat_id) VALUES (1, 1);
-- order 2 (paid) purchased seat 3 (A3 of show 1) and B1
-- order 2 (paid) purchased seat 3 (A3 of show 1) and B1
INSERT INTO order_seat (order_id, seat_id) VALUES (2, 3);
INSERT INTO order_seat (order_id, seat_id) VALUES (2, 4);
-- order 3 (cancelled) had seat A2 of show 2
-- order 3 (cancelled) had seat A2 of show 2
INSERT INTO order_seat (order_id, seat_id) VALUES (3, 7);
-- order 4 (paid) purchased seat B1 of show 2
-- order 4 (paid) purchased seat B1 of show 2
INSERT INTO order_seat (order_id, seat_id) VALUES (4, 8);

-- Additional users/orders for concurrency testing
-- Additional users/orders for concurrency testing
INSERT INTO orders (user_id, show_id, status) VALUES (200, 1, 'PENDING');
INSERT INTO orders (user_id, show_id, status) VALUES (201, 1, 'PENDING');
INSERT INTO order_seat (order_id, seat_id) VALUES (5, 2);
INSERT INTO order_seat (order_id, seat_id) VALUES (6, 5);

-- Notes:
-- * Enums (SeatStatus, OrderStatus) are persisted as ORDINAL (INT) by default in JPA for this project.
--   Values: SeatStatus: AVAILABLE=0, SOLD=1; OrderStatus: PENDING=0, PAID=1, CANCELLED=2
-- * H2 in application.yml runs in MySQL compatibility mode; this script uses MySQL-like syntax (AUTO_INCREMENT).
-- * For MySQL use, run this script against your test database. For H2 in-memory, Spring Data JPA can auto-create tables if configured, but this script is handy for manual verification.
