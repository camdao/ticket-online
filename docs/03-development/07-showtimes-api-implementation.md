# Showtimes API Implementation Summary

## Overview
This document summarizes the implementation of the Showtimes API endpoints as specified in `docs/02-design/03-api-design.md`.

## Implementation Date
July 14, 2026

## Implemented Components

### 1. Domain Layer

#### Entity: `Showtime`
- Location: `src/main/java/com/ticket_online/domain/showtimes/domain/Showtime.java`
- Maps to `shows` database table
- Relationships:
  - ManyToOne with Movie
  - ManyToOne with Cinema  
  - ManyToOne with Screen
- Fields: id, movieId, cinemaId, screenId, startTime, endTime, basePrice, status
- Status managed via `ShowtimeStatus` enum

#### Enum: `ShowtimeStatus`
- Location: `src/main/java/com/ticket_online/domain/showtimes/domain/ShowtimeStatus.java`
- Values: ACTIVE, CANCELLED, COMPLETED

### 2. Data Access Layer

#### Repository: `ShowtimeRepository`
- Location: `src/main/java/com/ticket_online/domain/showtimes/dao/ShowtimeRepository.java`
- Extends: `JpaRepository`, `JpaSpecificationExecutor`
- Custom queries:
  - `findByIdWithDetails()` - Fetch showtime with movie, cinema, and screen details
  - `findByMovieIdAndStatusAndStartTimeAfter()` - Get showtimes by movie
  - `findByCinemaIdAndStatusAndStartTimeAfter()` - Get showtimes by cinema

### 3. Application Layer

#### Service: `ShowtimeService`
- Location: `src/main/java/com/ticket_online/domain/showtimes/application/ShowtimeService.java`
- Key methods:
  - `searchShowtimes()` - Search with multiple filters using Specifications
  - `getShowtimeById()` - Get detailed showtime information
  - `getShowtimesByMovieId()` - Get showtimes for a specific movie
  - `getShowtimesByCinemaId()` - Get showtimes for a specific cinema

#### Dynamic Query Building
Uses Spring Data JPA Specifications to build dynamic queries based on provided filters:
- movieId
- cinemaId
- city (via cinema relationship)
- date (specific date)
- startDate/endDate (date range)

### 4. Presentation Layer

#### DTOs

**ShowtimeResponse**
- Location: `src/main/java/com/ticket_online/domain/showtimes/dto/response/ShowtimeResponse.java`
- Contains: showtime details, movie info, cinema info, screen info, availability

**ShowtimeDetailResponse**
- Location: `src/main/java/com/ticket_online/domain/showtimes/dto/response/ShowtimeDetailResponse.java`
- Contains: full nested objects for movie, cinema, and screen with all their properties

**ShowtimeListResponse**
- Location: `src/main/java/com/ticket_online/domain/showtimes/dto/response/ShowtimeListResponse.java`
- Contains: paginated list of showtimes with metadata

#### Controller: `ShowtimeController`
- Location: `src/main/java/com/ticket_online/domain/showtimes/api/ShowtimeController.java`
- Base path: `/api/v1/showtimes`

## Implemented API Endpoints

### GET /api/v1/showtimes
Search showtimes with filters.

**Query Parameters:**
- `movieId` (optional) - Filter by movie
- `cinemaId` (optional) - Filter by cinema
- `city` (optional) - Filter by city
- `date` (optional) - Specific date (YYYY-MM-DD)
- `startDate` (optional) - Start of date range
- `endDate` (optional) - End of date range
- `page` (default: 0) - Page number
- `size` (default: 20) - Page size

**Response:** `ShowtimeListResponse` with pagination

### GET /api/v1/showtimes/{id}
Get detailed showtime information.

**Path Variable:**
- `id` - Showtime ID

**Response:** `ShowtimeDetailResponse` with nested movie, cinema, and screen details

## Integration with Other Domains

### Movies Domain
**Endpoint:** `GET /api/v1/movies/{id}/showtimes`
- Added to: `src/main/java/com/ticket_online/domain/movies/api/MovieController.java`
- Service method: `MovieService.getMovieShowtimes()`
- Delegates to: `ShowtimeService.getShowtimesByMovieId()`

### Cinemas Domain
**Endpoint:** `GET /api/v1/cinemas/{id}/showtimes`
- Added to: `src/main/java/com/ticket_online/domain/cinemas/api/CinemaController.java`
- Service method: `CinemaService.getCinemaShowtimes()`
- Delegates to: `ShowtimeService.getShowtimesByCinemaId()`

## Error Handling

Added error code to `ErrorCode` enum:
- `SHOWTIME_NOT_FOUND` - HTTP 404 when showtime doesn't exist

## Key Design Decisions

1. **Dynamic Filtering with Specifications**
   - Used Spring Data JPA Specifications for flexible query building
   - Allows combining multiple filter criteria dynamically
   - More maintainable than creating separate repository methods for each combination

2. **Eager Loading with JOIN FETCH**
   - Prevents N+1 query problems
   - Ensures all related entities are loaded in a single query
   - Critical for performance when returning lists of showtimes

3. **Separate Response DTOs**
   - `ShowtimeResponse` for list views (optimized size)
   - `ShowtimeDetailResponse` for detail view (complete information)
   - Follows API design specification precisely

4. **Cross-Domain Integration**
   - Showtimes accessible through movie and cinema endpoints
   - Maintains single source of truth in ShowtimeService
   - Clean separation of concerns

5. **Date Handling**
   - Supports both specific date and date range filtering
   - Uses `LocalDateTime` for precise time handling
   - Automatic conversion from String parameters

## Database Schema Requirements

The implementation assumes the following database schema:

```sql
CREATE TABLE shows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    movie_id BIGINT NOT NULL,
    cinema_id BIGINT NOT NULL,
    screen_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (cinema_id) REFERENCES cinemas(id),
    FOREIGN KEY (screen_id) REFERENCES rooms(id),
    INDEX idx_movie_start (movie_id, start_time),
    INDEX idx_cinema_start (cinema_id, start_time),
    INDEX idx_status_start (status, start_time)
);
```

## Testing Recommendations

1. **Unit Tests**
   - Test Specification building logic
   - Test ShowtimeService filtering methods
   - Mock repository responses

2. **Integration Tests**
   - Test actual database queries
   - Verify JOIN FETCH works correctly
   - Test pagination

3. **API Tests**
   - Test all filter combinations
   - Test pagination parameters
   - Test error responses (404 for non-existent showtimes)
   - Test integration with /movies/{id}/showtimes and /cinemas/{id}/showtimes

## Performance Considerations

1. **Indexes**: Database indexes on movie_id, cinema_id, start_time, and status are critical
2. **Eager Loading**: JOIN FETCH prevents N+1 queries but loads more data - appropriate for showtime listings
3. **Pagination**: Default page size of 20 prevents excessive data loading
4. **Query Optimization**: Specifications are compiled to efficient SQL with proper WHERE clauses

## Future Enhancements

1. **Caching**: Consider caching frequently accessed showtime lists
2. **Seat Availability**: Currently returns calculated available seats - could be optimized with Redis
3. **Time Zone Support**: Add timezone handling for international deployments
4. **Advanced Filtering**: Add sorting options, screen type filter, price range filter
5. **Bulk Operations**: Admin endpoints for creating/updating multiple showtimes

## Compliance with API Design

This implementation fully complies with the API design specification in `docs/02-design/03-api-design.md`:

✅ GET /showtimes with all specified query parameters
✅ GET /showtimes/{id} with nested response structure
✅ GET /movies/{id}/showtimes integration
✅ GET /cinemas/{id}/showtimes integration
✅ Response format matches specification
✅ Pagination support
✅ Error handling with appropriate HTTP status codes

## Build Status

✅ Project builds successfully with `./gradlew build`
✅ Code formatting validated with Spotless
✅ No compilation errors