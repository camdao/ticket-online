--  data for k6 load testing
-- Create seats
INSERT INTO seats (show_id, seat_code, status) VALUES 
(1, 'A1', 'AVAILABLE'), (1, 'A2', 'AVAILABLE'), (1, 'A3', 'AVAILABLE'), (1, 'A4', 'AVAILABLE'), (1, 'A5', 'AVAILABLE'),
(1, 'A6', 'AVAILABLE'), (1, 'A7', 'AVAILABLE'), (1, 'A8', 'AVAILABLE'), (1, 'A9', 'AVAILABLE'), (1, 'A10', 'AVAILABLE'),
(1, 'B1', 'AVAILABLE'), (1, 'B2', 'AVAILABLE'), (1, 'B3', 'AVAILABLE'), (1, 'B4', 'AVAILABLE'), (1, 'B5', 'AVAILABLE'),
(1, 'B6', 'AVAILABLE'), (1, 'B7', 'AVAILABLE'), (1, 'B8', 'AVAILABLE'), (1, 'B9', 'AVAILABLE'), (1, 'B10', 'AVAILABLE'),
(1, 'C1', 'AVAILABLE'), (1, 'C2', 'AVAILABLE'), (1, 'C3', 'AVAILABLE'), (1, 'C4', 'AVAILABLE'), (1, 'C5', 'AVAILABLE'),
(1, 'C6', 'AVAILABLE'), (1, 'C7', 'AVAILABLE'), (1, 'C8', 'AVAILABLE'), (1, 'C9', 'AVAILABLE'), (1, 'C10', 'AVAILABLE'),
(1, 'D1', 'AVAILABLE'), (1, 'D2', 'AVAILABLE'), (1, 'D3', 'AVAILABLE'), (1, 'D4', 'AVAILABLE'), (1, 'D5', 'AVAILABLE'),
(1, 'D6', 'AVAILABLE'), (1, 'D7', 'AVAILABLE'), (1, 'D8', 'AVAILABLE'), (1, 'D9', 'AVAILABLE'), (1, 'D10', 'AVAILABLE'),
(1, 'E1', 'AVAILABLE'), (1, 'E2', 'AVAILABLE'), (1, 'E3', 'AVAILABLE'), (1, 'E4', 'AVAILABLE'), (1, 'E5', 'AVAILABLE'),
(1, 'E6', 'AVAILABLE'), (1, 'E7', 'AVAILABLE'), (1, 'E8', 'AVAILABLE'), (1, 'E9', 'AVAILABLE'), (1, 'E10', 'AVAILABLE'),
(1, 'F1', 'AVAILABLE'), (1, 'F2', 'AVAILABLE'), (1, 'F3', 'AVAILABLE'), (1, 'F4', 'AVAILABLE'), (1, 'F5', 'AVAILABLE'),
(1, 'F6', 'AVAILABLE'), (1, 'F7', 'AVAILABLE'), (1, 'F8', 'AVAILABLE'), (1, 'F9', 'AVAILABLE'), (1, 'F10', 'AVAILABLE'),
(1, 'G1', 'AVAILABLE'), (1, 'G2', 'AVAILABLE'), (1, 'G3', 'AVAILABLE'), (1, 'G4', 'AVAILABLE'), (1, 'G5', 'AVAILABLE'),
(1, 'G6', 'AVAILABLE'), (1, 'G7', 'AVAILABLE'), (1, 'G8', 'AVAILABLE'), (1, 'G9', 'AVAILABLE'), (1, 'G10', 'AVAILABLE'),
(1, 'H1', 'AVAILABLE'), (1, 'H2', 'AVAILABLE'), (1, 'H3', 'AVAILABLE'), (1, 'H4', 'AVAILABLE'), (1, 'H5', 'AVAILABLE'),
(1, 'H6', 'AVAILABLE'), (1, 'H7', 'AVAILABLE'), (1, 'H8', 'AVAILABLE'), (1, 'H9', 'AVAILABLE'), (1, 'H10', 'AVAILABLE'),
(1, 'I1', 'AVAILABLE'), (1, 'I2', 'AVAILABLE'), (1, 'I3', 'AVAILABLE'), (1, 'I4', 'AVAILABLE'), (1, 'I5', 'AVAILABLE'),
(1, 'I6', 'AVAILABLE'), (1, 'I7', 'AVAILABLE'), (1, 'I8', 'AVAILABLE'), (1, 'I9', 'AVAILABLE'), (1, 'I10', 'AVAILABLE'),
(1, 'J1', 'AVAILABLE'), (1, 'J2', 'AVAILABLE'), (1, 'J3', 'AVAILABLE'), (1, 'J4', 'AVAILABLE'), (1, 'J5', 'AVAILABLE'),
(1, 'J6', 'AVAILABLE'), (1, 'J7', 'AVAILABLE'), (1, 'J8', 'AVAILABLE'), (1, 'J9', 'AVAILABLE'), (1, 'J10', 'AVAILABLE');

-- Create show
INSERT INTO shows (id, name, created_at, updated_at) VALUES 
(1, 'Test Concert', NOW(), NOW());

