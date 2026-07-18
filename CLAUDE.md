# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ticket Online is a cinema ticket booking system built with Spring Boot. The core technical challenge is handling concurrent seat reservations: preventing overselling, managing temporary seat holds with Redis TTL, and ensuring payment idempotency during high traffic.

## Essential Commands

### Build and Run
```bash
./gradlew build              # Build project
./gradlew bootRun            # Run application
./gradlew build -x test      # Build without tests
./gradlew clean build        # Clean build
```

### Code Formatting
```bash
./gradlew spotlessCheck      # Check formatting (Google Java Format AOSP)
./gradlew spotlessApply      # Apply formatting
```

**Note**: Pre-commit hook automatically runs `spotlessApply` on staged files. Hook installed via `updateGitHooks` task on every compile.

### Testing
```bash
./gradlew test                                                    # All tests
./gradlew test --tests "com.ticket_online.domain.user.*Test"    # Package
./gradlew test --tests "UserServiceTest"                         # Specific class
./gradlew test --tests "*ServiceTest"                            # Pattern
```

### Database
```bash
docker-compose -f docker-compose-test.yaml up   # Start test database
mysql -u root -p < init.sql                     # Initialize MySQL schema
```

## Architecture

### Domain Structure

Each domain follows a strict layered pattern:
- **`api/`** - REST controllers
- **`application/`** - Service layer with business logic
- **`dao/`** - JPA repositories
- **`domain/`** - Entity classes with business methods
- **`dto/request/` and `dto/response/`** - Data transfer objects

**Domains**: `auth`, `bookings`, `cinemas`, `movies`, `payments`, `rooms`, `seats`, `showtimes`, `user`

### Global Infrastructure (`global/`)

- **`config/`** - Spring configuration (Redis, Security, JPA, Swagger, VNPay)
- **`security/`** - `JwtAuthenticationFilter`, `PrincipalDetails`
- **`error/`** - `GlobalExceptionHandler`, `CustomException`, `ErrorCode` enum
- **`util/`** - `RedisSeatScripts` (critical for seat operations), `JwtUtil`, `SecurityUtil`, `UserUtil`, `CookieUtil`

## Critical Patterns

### Redis Seat Reservation

**All seat operations MUST use `RedisSeatScripts` Lua scripts** to maintain atomicity and prevent race conditions.

Key methods in `global/util/RedisSeatScripts.java`:
- **`holdSeats(seatIds, showId, userId, ttlSeconds)`** - Atomically checks availability and sets holds with TTL
- **`checkAndExtendSeats(showId, seatIds, userId, ttlSeconds)`** - Verifies ownership and extends expiration
- **`releaseSeats(showId, seatIds)`** - Deletes holds to free seats

Redis key format: `seat:hold:{showId}:{seatId}` → value is `userId`

**Why Lua scripts**: Multiple operations must be atomic. Checking then setting in separate Redis calls creates race conditions where two users can book the same seat.

### JWT Authentication Flow

`JwtAuthenticationFilter` handles authentication in this order:
1. Check `Authorization: Bearer <token>` header
2. Check access token in cookie
3. If access token expired but refresh token valid → automatically reissue both tokens, set new cookies in response
4. Set `SecurityContext` with user info

**Getting current user**: Use `SecurityUtil.getCurrentUserId()` or `UserUtil.getCurrentUser()` in services.

### Database Patterns

**BaseTimeEntity**: All entities inherit from this for automatic `createdAt`/`updatedAt` timestamps.

**Soft Deletes**: Entities use `isActive` flag:
```java
entity.deactivate();  // Don't delete, set isActive = false
entity.activate();    // Restore
```

Repository methods should filter by active state (e.g., `findActiveByScreenId()`).

**QueryDSL**: Type-safe queries via annotation processing. Q-classes generated automatically.

### Error Handling

Throw `CustomException` with `ErrorCode` enum:
```java
throw new CustomException(ErrorCode.SEAT_ALREADY_HELD);
```

`GlobalExceptionHandler` converts to standardized JSON error responses.

### Service Layer Pattern

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Default for class
@Slf4j
public class ExampleService {
    
    @Transactional  // Override for write operations
    public void writeMethod() { }
}
```

### Response DTOs

Use static factory methods:
```java
public static SeatResponse from(Seat seat) { }
public static ScreenLayoutResponse of(List<String> rows, Integer maxSeats) { }
```

## Configuration

Spring profiles included via `application.yml`:
- `application-datasource.yml` - H2 for tests, MySQL for production
- `application-redis.yml` - Redis connection
- `application-security.yml` - JWT settings

## Payment Integration

VNPay configuration in `application.yml` under `vnpay:` section. `VnpayService` handles:
- Payment URL generation
- Callback verification
- Idempotency to prevent duplicate payments

## API Documentation

Swagger UI: `/swagger-ui` when running. Config in `global/config/swagger/SwaggerConfig.java`.

## Monitoring

Prometheus metrics at `/actuator/prometheus`. Health check at `/actuator/health`.

## Additional Documentation

Vietnamese documentation in `docs/`:
- `00-project-overview.md` - Business requirements and technical challenges
- `02-design/` - System design, database design, seat reservation design, payment design
- `03-development/` - Implementation guides per domain
