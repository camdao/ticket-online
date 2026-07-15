# Seats (Room) API Implementation

## Overview

This document describes the implementation of the Seats API for the ticket-online system. The Seats API manages individual seats within cinema screens (rooms), including seat layout, types, and availability.

**Note:** In the database design, "rooms" refer to cinema screening rooms, which are mapped to the `Screen` entity in the codebase. The `Seat` entity represents individual seats within these screens.

## Domain Structure

### Entities

#### 1. Seat Entity
**Location:** `src/main/java/com/ticket_online/domain/seats/domain/Seat.java`

**Fields:**
- `id`: Long - Primary key
- `screen`: Screen - Reference to the cinema screen (room)
- `row`: String - Seat row (A, B, C, etc.)
- `number`: Integer - Seat number within the row
- `type`: SeatType - Type of seat (REGULAR, VIP, COUPLE)
- `basePrice`: Long - Base price for this seat
- `isActive`: Boolean - Whether the seat is active

**Key Methods:**
- `getSeatLabel()`: Returns formatted seat label (e.g., "A-5")
- `calculatePrice()`: Returns the seat price
- `activate()/deactivate()`: Manage seat availability
- `updateType()`: Change seat type
- `updateBasePrice()`: Update seat pricing

**Database Constraints:**
- Unique constraint on (screen_id, row, number)
- Index on screen_id
- Index on (row, number)

#### 2. SeatType Enum
**Location:** `src/main/java/com/ticket_online/domain/seats/domain/SeatType.java`

**Values:**
- `REGULAR`: Standard seat
- `VIP`: Premium seat with higher pricing
- `COUPLE`: Double seat for two people

#### 3. SeatStatus Enum
**Location:** `src/main/java/com/ticket_online/domain/seats/domain/SeatStatus.java`

**Values:**
- `AVAILABLE`: Seat is available for booking
- `HELD`: Temporarily held by a user (not yet confirmed)
- `BOOKED`: Confirmed booking

**Note:** Seat status is not stored in the database. It's calculated at runtime based on bookings and Redis hold data.

### Repository

**Location:** `src/main/java/com/ticket_online/domain/seats/dao/SeatRepository.java`

**Key Methods:**
- `findByScreenId()`: Get all seats for a screen
- `findActiveByScreenId()`: Get only active seats
- `findByScreenIdAndRowAndNumber()`: Find specific seat
- `findByScreenIdAndType()`: Filter by seat type
- `countByScreenId()`: Count seats in a screen
- `findByIdIn()`: Get seats by IDs
- `existsByScreenIdAndRowAndNumber()`: Check seat existence

### DTOs

#### Request DTOs

**CreateSeatRequest:**
```java
{
    "screenId": Long,
    "row": String,        // 1-2 uppercase letters
    "number": Integer,    // >= 1
    "type": SeatType,
    "basePrice": Long     // >= 1000
}
```

**UpdateSeatRequest:**
```java
{
    "type": SeatType,     // optional
    "basePrice": Long,    // optional, >= 1000
    "isActive": Boolean   // optional
}
```

#### Response DTOs

**SeatResponse:**
```java
{
    "id": Long,
    "row": String,
    "number": Integer,
    "type": SeatType,
    "price": Long,
    "status": SeatStatus
}
```

**ScreenLayoutResponse:**
```java
{
    "rows": List<String>,      // e.g., ["A", "B", "C"]
    "seatsPerRow": Integer     // Max seats per row
}
```

### Service Layer

**Location:** `src/main/java/com/ticket_online/domain/seats/application/SeatService.java`

**Key Methods:**
- `getSeatsByScreenId()`: Retrieve all seats for a screen
- `getScreenLayout()`: Get screen layout information
- `createSeat()`: Create a new seat
- `updateSeat()`: Update seat properties
- `deleteSeat()`: Soft delete a seat (deactivate)
- `bulkCreateSeats()`: Create multiple seats at once
- `getSeatsByIds()`: Get seats by their IDs

**Business Logic:**
- Validates screen existence before operations
- Prevents duplicate seats (same screen, row, number)
- Supports soft delete through deactivation
- Provides bulk creation for initial screen setup

### Controller Layer

**Location:** `src/main/java/com/ticket_online/domain/seats/api/SeatController.java`

**Base URL:** `/api/v1/seats`

## API Endpoints

### 1. Get Seats by Screen
```http
GET /api/v1/seats?screenId={screenId}
```

**Description:** Retrieves all active seats for a specific screen.

**Query Parameters:**
- `screenId` (required): ID of the screen

**Response (200):**
```json
[
    {
        "id": 1201,
        "row": "A",
        "number": 1,
        "type": "REGULAR",
        "price": 85000,
        "status": "AVAILABLE"
    },
    {
        "id": 1202,
        "row": "A",
        "number": 2,
        "type": "VIP",
        "price": 120000,
        "status": "AVAILABLE"
    }
]
```

### 2. Get Screen Layout
```http
GET /api/v1/seats/layout?screenId={screenId}
```

**Description:** Gets the layout information for a screen.

**Query Parameters:**
- `screenId` (required): ID of the screen

**Response (200):**
```json
{
    "rows": ["A", "B", "C", "D", "E", "F", "G", "H"],
    "seatsPerRow": 15
}
```

### 3. Create Seat
```http
POST /api/v1/seats
```

**Description:** Creates a new seat in a screen.

**Request Body:**
```json
{
    "screenId": 12,
    "row": "A",
    "number": 1,
    "type": "REGULAR",
    "basePrice": 85000
}
```

**Response (201):**
```json
{
    "id": 1201,
    "row": "A",
    "number": 1,
    "type": "REGULAR",
    "price": 85000,
    "status": "AVAILABLE"
}
```

### 4. Update Seat
```http
PUT /api/v1/seats/{id}
```

**Description:** Updates seat properties.

**Path Parameters:**
- `id`: Seat ID

**Request Body:**
```json
{
    "type": "VIP",
    "basePrice": 120000,
    "isActive": true
}
```

**Response (200):**
```json
{
    "id": 1201,
    "row": "A",
    "number": 1,
    "type": "VIP",
    "price": 120000,
    "status": "AVAILABLE"
}
```

### 5. Delete Seat
```http
DELETE /api/v1/seats/{id}
```

**Description:** Soft deletes a seat by deactivating it.

**Path Parameters:**
- `id`: Seat ID

**Response (204):** No Content

### 6. Bulk Create Seats
```http
POST /api/v1/seats/bulk?screenId={screenId}&rows={rows}&seatsPerRow={seatsPerRow}&type={type}&basePrice={basePrice}
```

**Description:** Creates multiple seats at once for initial screen setup.

**Query Parameters:**
- `screenId`: Screen ID
- `rows`: Array of row labels (e.g., ["A", "B", "C"])
- `seatsPerRow`: Number of seats per row
- `type`: Default seat type
- `basePrice`: Base price for all seats

**Response (201):**
```json
[
    {
        "id": 1201,
        "row": "A",
        "number": 1,
        "type": "REGULAR",
        "price": 85000,
        "status": "AVAILABLE"
    },
    // ... more seats
]
```

## Error Handling

### New Error Codes Added

- `SEAT_ALREADY_EXISTS`: When trying to create a seat at an occupied position
- `SEAT_NOT_AVAILABLE`: When a seat is not available for booking
- `SEATS_NOT_FOUND`: When one or more requested seats don't exist

Existing related error codes:
- `SEAT_NOT_FOUND`: Seat doesn't exist
- `SEAT_HOLD_FAILED`: Failed to hold seat
- `SEAT_ALREADY_SOLD`: Seat is already booked
- `SEAT_ALREADY_HELD`: Seat is held by another user
- `SCREEN_NOT_FOUND`: Screen doesn't exist

## Integration Points

### With Screen/Room Entity
- Each seat belongs to a screen (room)
- Screen validation is performed before seat operations
- Seats are queried by screen ID

### With Booking System
- Seat IDs are used in booking operations
- Seat status is calculated based on bookings
- The `getSeatsByIds()` method supports booking validation

### With Redis Hold System
- Seat holds are managed in Redis (not database)
- Seat status reflects both database bookings and Redis holds
- Integration point for showtime seat availability

## Usage Examples

### Creating a Screen with Seats

```java
// 1. Create the screen first (via ScreenService)
// 2. Bulk create seats for the screen
bulkCreateSeats(
    screenId: 12,
    rows: ["A", "B", "C", "D", "E"],
    seatsPerRow: 15,
    type: REGULAR,
    basePrice: 85000
);

// 3. Update specific seats to VIP
updateSeat(seatId, {
    type: "VIP",
    basePrice: 120000
});
```

### Getting Available Seats for Booking

```java
// Get all seats for a screen
List<SeatResponse> seats = getSeatsByScreenId(screenId);

// Filter for available seats
List<SeatResponse> available = seats.stream()
    .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
    .collect(Collectors.toList());
```

## Database Schema

```sql
CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screen_id BIGINT NOT NULL,
    row VARCHAR(2) NOT NULL,
    number INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    base_price BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (screen_id) REFERENCES rooms(id),
    UNIQUE KEY unique_seat (screen_id, row, number),
    KEY idx_seat_screen (screen_id),
    KEY idx_seat_row_number (row, number)
);
```

## Testing Considerations

1. **Unit Tests:**
   - Seat creation with validation
   - Seat update logic
   - Soft delete functionality
   - Bulk creation

2. **Integration Tests:**
   - Screen-seat relationship
   - Duplicate seat prevention
   - Seat status calculation with bookings

3. **Edge Cases:**
   - Creating seat in non-existent screen
   - Duplicate seat positions
   - Invalid row/number formats
   - Price validation

## Future Enhancements

1. **Seat Pricing Rules:**
   - Time-based pricing (weekday vs weekend)
   - Screen-type surcharges (IMAX, 4DX)
   - Dynamic pricing based on demand

2. **Seat Groups:**
   - Define seat groups (front, middle, back)
   - Group-based pricing

3. **Accessibility:**
   - Wheelchair accessible seats
   - Companion seats

4. **Maintenance Mode:**
   - Temporarily disable seats for maintenance
   - Bulk operations for seat maintenance

## Related Documentation

- [Database Design](../02-design/02-database-design.md)
- [API Design](../02-design/03-api-design.md)
- [Seat Reservation Design](../02-design/04-seat-reservation-design.md)
- [Cinemas Domain Implementation](04-cinemas-domain-implementation.md)
- [Showtimes API Implementation](07-showtimes-api-implementation.md)