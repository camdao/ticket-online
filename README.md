# 🎬 Ticket Online - Cinema Booking System

A high-performance cinema ticket booking system built with Spring Boot, designed to handle concurrent seat reservations and high-traffic scenarios.

## 🧑‍💻 Team

| Profile | Name | Role |
| :---: | :---: | :---: |
| <a href="https://github.com/camdao"><img src="https://avatars.githubusercontent.com/camdao" height="120px"></a> | camdao | Backend Engineer |

## 🎯 Project Overview

Ticket Online is an enterprise-grade cinema ticket booking system that solves critical concurrency challenges in real-time seat reservation. The system prevents overselling, manages temporary seat holds with Redis TTL, and ensures payment idempotency during peak traffic periods.

### Key Technical Challenges
- **Concurrent Seat Reservation**: Atomic operations using Redis Lua scripts to prevent race conditions
- **Temporary Hold Management**: TTL-based seat holds with automatic expiration and extension
- **Payment Idempotency**: Prevent duplicate payments during network retries and high traffic
- **High Availability**: Designed for scalability with stateless architecture and distributed caching

## 📆 Development Period
- ***2024.XX.XX ~ Present***

## 💻 Tech Stack

### Backend
> Language: ```Java 17``` <br>
> Framework: ```Spring Boot 3.3.4``` <br>
> Database: ```MySQL 8.x``` <br>
> Cache: ```Redis 7.x``` <br>
> ORM: ```JPA (Hibernate) + QueryDSL``` <br>
> Build Tool: ```Gradle 8.x``` <br>

### Infrastructure
> Containerization: ```Docker, Docker Compose``` <br>
> CI/CD: ```GitHub Actions``` <br>
> Testing: ```JUnit 5, Testcontainers``` <br>
> Monitoring: ```Prometheus, Grafana (Actuator)``` <br>
> API Documentation: ```Swagger/OpenAPI 3.0```

### External Integrations
> Payment: ```VNPay``` <br>
> Authentication: ```JWT, Spring Security```

## 🏗️ Architecture

### System Architecture
```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   Client App    │──────▶│  Spring Boot    │──────▶│     MySQL       │
│   (Web/Mobile)  │      │   Application   │      │   Database      │
└─────────────────┘      └─────────────────┘      └─────────────────┘
                               │       │
                               │       │
                               ▼       ▼
                         ┌──────────┐ ┌──────────┐
                         │  Redis   │ │  VNPay   │
                         │  Cache   │ │ Payment  │
                         └──────────┘ └──────────┘
```

### Domain-Driven Design Structure
```
ticket-online/
├── domain/
│   ├── auth/          # Authentication & JWT
│   ├── bookings/      # Booking management
│   ├── cinemas/       # Cinema entities
│   ├── movies/        # Movie catalog
│   ├── payments/      # VNPay integration
│   ├── rooms/         # Screening rooms
│   ├── seats/         # Seat management
│   ├── showtimes/     # Show scheduling
│   └── user/          # User management
├── global/
│   ├── config/        # Spring configurations
│   ├── security/      # JWT filters & auth
│   ├── error/         # Global exception handling
│   └── util/          # Redis scripts, JWT utils
└── infra/
    └── redis/         # Redis configuration
```

Each domain follows a strict layered pattern:
- **`api/`** - REST controllers (presentation layer)
- **`application/`** - Business logic (service layer)
- **`dao/`** - JPA repositories (data access)
- **`domain/`** - Entity classes with business methods
- **`dto/request/`, `dto/response/`** - Data transfer objects

## 🚀 Quick Start

### Prerequisites
```bash
# Required
- Java 17+
- Docker & Docker Compose
- MySQL 8.x (or use Docker Compose)
- Redis 7.x (or use Docker Compose)
```

### Installation & Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd ticket-online
```

2. **Start infrastructure services**
```bash
# Start MySQL and Redis using Docker Compose
docker-compose -f docker-compose-test.yaml up -d
```

3. **Initialize database**
```bash
# Run MySQL initialization script
mysql -u root -p < init.sql
```

4. **Configure application**
```bash
# Edit application.yml with your settings
# - Database credentials
# - Redis connection
# - VNPay API keys
# - JWT secret
```

5. **Build and run**
```bash
# Build the project
./gradlew clean build

# Run the application
./gradlew bootRun
```

6. **Access the application**
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui`
- Health Check: `http://localhost:8080/actuator/health`

## 📝 Essential Commands

### Build & Run
```bash
./gradlew build              # Build project
./gradlew bootRun            # Run application
./gradlew build -x test      # Build without tests
./gradlew clean build        # Clean build
```

### Code Quality
```bash
./gradlew spotlessCheck      # Check formatting (Google Java Format AOSP)
./gradlew spotlessApply      # Apply formatting
```
**Note**: Pre-commit hook automatically runs `spotlessApply` on staged files.

### Testing
```bash
./gradlew test                                                    # All tests
./gradlew test --tests "com.ticket_online.domain.user.*Test"    # Package tests
./gradlew test --tests "UserServiceTest"                         # Specific class
./gradlew test --tests "*ServiceTest"                            # Pattern matching
```

### Database
```bash
docker-compose -f docker-compose-test.yaml up    # Start test database
docker-compose -f docker-compose-test.yaml down  # Stop test database
mysql -u root -p < init.sql                      # Initialize MySQL schema
```

## 🔑 Key Features

### 1. Atomic Seat Reservation with Redis Lua Scripts
Prevents race conditions in concurrent bookings using Redis Lua scripts for atomic operations:

```java
// RedisSeatScripts.java
holdSeats(seatIds, showId, userId, ttlSeconds)       // Atomic check + hold
releaseSeats(showId, seatIds)                        // Free seats
```

**Redis Key Pattern**: `seat:hold:{showId}:{seatId}` → value: `userId`

### 2. JWT Authentication with Auto-Refresh
Seamless token refresh without user interruption:
1. Check `Authorization: Bearer <token>` header
2. Check access token in HTTP-only cookie
3. Auto-reissue both tokens if access expired but refresh valid
4. Set new cookies in response

### 3. VNPay Payment Integration
- Secure payment URL generation
- Callback verification with HMAC signatures
- Idempotency to prevent duplicate charges

### 4. Soft Delete Pattern
All entities use `isActive` flag for data retention:
```java
entity.deactivate();  // Soft delete
entity.activate();    // Restore
```

### 5. Global Error Handling
Standardized error responses with custom error codes:
```java
throw new CustomException(ErrorCode.SEAT_ALREADY_HELD);
// → Handled by GlobalExceptionHandler
```

## 📊 Testing Strategy

### TDD Approach
- **Target**: 80%+ statement coverage
- **Unit Tests**: Service layer with mocked dependencies
- **Integration Tests**: Repository layer with Testcontainers
- **Database Idempotency**: Testcontainers ensures clean state per test

### Test Results
```
Statement Coverage:  88%
Branch Coverage:     54.8%
Class Coverage:      100%
Method Coverage:     96.7%
```

## 📈 Performance & Monitoring

### Monitoring Stack
- **Prometheus**: Metrics collection (`/actuator/prometheus`)
- **Grafana**: Visualization and alerting
- **Spring Actuator**: Health checks and operational endpoints

### Performance Targets
- Seat reservation: < 500ms (p95)
- Payment processing: < 2s (p95)
- Search queries: < 200ms (p95)

## 🔐 Security

### Implemented Security Measures
- ✅ JWT-based authentication with secure cookie storage
- ✅ Spring Security for authorization and URI access control
- ✅ HTTPS/TLS for data in transit (production)
- ✅ SQL injection prevention via JPA parameterized queries
- ✅ CSRF protection with token validation
- ✅ XSS protection with response header configuration
- ✅ Password encryption with BCrypt

### Security Best Practices
- Access tokens stored in HTTP-only cookies (XSS protection)
- Refresh tokens have longer expiry and auto-rotation
- Sensitive endpoints protected with role-based access control
- Payment callbacks validated with HMAC signatures

## 🗄️ Database Design

### Core Entities
- **Users**: User accounts and authentication
- **Movies**: Movie catalog with metadata
- **Cinemas**: Theater locations
- **Rooms**: Screening rooms with seat layouts
- **Seats**: Individual seat entities
- **Showtimes**: Movie schedules linked to rooms
- **Bookings**: Reservation records
- **Payments**: Transaction history with VNPay

### Design Principles
- Normalized schema (3NF) for data integrity
- Soft deletes with `isActive` flag
- Optimistic locking for concurrent updates
- Indexed foreign keys for query performance
- `BaseTimeEntity` for automatic timestamp management

## 📚 API Documentation

### Swagger UI
Access interactive API documentation at `/swagger-ui` when the application is running.

### Key Endpoints
```
POST   /api/auth/login           # User authentication
POST   /api/auth/refresh         # Token refresh
GET    /api/movies               # List movies
GET    /api/showtimes            # Show schedules
POST   /api/bookings/hold        # Hold seats (temporary)
POST   /api/bookings/confirm     # Confirm booking
POST   /api/payments/vnpay       # Initiate VNPay payment
GET    /api/payments/callback    # VNPay callback handler
```

## 🚢 Deployment

### Docker Deployment
```bash
# Build Docker image
docker build -t ticket-online:latest .

# Run with Docker Compose
docker-compose up -d
```

### CI/CD Pipeline
GitHub Actions workflow:
1. ✅ Run tests and code quality checks
2. ✅ Build application artifact
3. ✅ Build and push Docker image
4. ✅ Deploy to production server via SSH

## 🐛 Troubleshooting

### Common Issues

**Redis Connection Failed**
```bash
# Check Redis is running
docker ps | grep redis
# Restart Redis container
docker-compose restart redis
```

**Database Migration Issues**
```bash
# Reset test database
docker-compose down -v
docker-compose up -d
mysql -u root -p < init.sql
```

**Tests Failing**
```bash
# Clean build and re-run tests
./gradlew clean test
```

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Run spotless before committing: `./gradlew spotlessApply`
3. Ensure tests pass: `./gradlew test`
4. Commit with meaningful messages
5. Push and create a Pull Request

## 📄 License

This project is proprietary software. All rights reserved.

## 📞 Contact

For questions or support, please contact the development team.

---

**Built with ❤️ using Spring Boot**
