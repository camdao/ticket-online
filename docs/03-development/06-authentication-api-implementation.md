# Authentication API Implementation

## Overview
This document describes the implementation of the Authentication API endpoints according to the API design specification.

## Implemented Endpoints

### 1. POST /api/v1/auth/register
Registers a new user account.

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "phoneNumber": "0912345678"
}
```

**Response (201 CREATED):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Validations:**
- Username: 3-50 characters, unique
- Email: valid email format, unique
- Password: minimum 8 characters
- Phone number: 10 digits starting with 0
- All fields are required

### 2. POST /api/v1/auth/login
Authenticates a user and returns access and refresh tokens.

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 3. POST /api/v1/auth/logout
Logs out the current user by invalidating the refresh token.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "message": "Logout successful"
}
```

**Security:** Requires authentication

### 4. POST /api/v1/auth/refresh
Refreshes the access token using a valid refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## Implementation Details

### Data Model Changes

#### User Entity
Updated `User.java` with the following fields:
- `username` (String, unique, not null)
- `email` (String, unique, not null, email format)
- `password` (String, not null, encrypted)
- `fullName` (String, not null)
- `phoneNumber` (String, not null, pattern: ^0\d{9}$)
- `role` (UserRole enum, defaults to ROLE_USER)

#### UserRepository
Added method:
- `Optional<User> findByEmail(String email)`

### DTOs

#### Request DTOs
1. **RegisterRequest**
   - username, email, password, fullName, phoneNumber
   - Full validation annotations

2. **RefreshTokenRequest**
   - refreshToken (not blank)

#### Response DTOs
1. **TokenPairResponse**
   - accessToken, refreshToken, tokenType, expiresIn
   - Static factory method: `from(accessToken, refreshToken, expiresIn)`

2. **AccessTokenResponse**
   - accessToken, tokenType, expiresIn
   - Static factory method: `from(accessToken, expiresIn)`

### Service Layer

#### AuthService
Implemented methods:
1. **register(RegisterRequest)** - Creates new user account
   - Validates username and email uniqueness
   - Encrypts password using BCrypt
   - Generates JWT tokens
   - Returns TokenPairResponse

2. **login(UsernamePasswordRequest)** - Authenticates user
   - Validates credentials
   - Generates JWT tokens
   - Returns TokenPairResponse

3. **logout(Long userId)** - Invalidates refresh token
   - Deletes refresh token from Redis
   - Client must discard access token

4. **refreshAccessToken(RefreshTokenRequest)** - Refreshes access token
   - Validates refresh token from Redis
   - Generates new access token
   - Returns AccessTokenResponse

### Controller Layer

#### AuthController
- Base path: `/api/v1/auth`
- All endpoints return appropriate HTTP status codes
- Sets secure HTTP-only cookies for tokens
- Handles validation errors through global exception handler

### Security Configuration

#### WebSecurityConfig
Updated endpoint permissions:
- **Public access:** `/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/auth/refresh`
- **Authenticated access:** `/api/v1/auth/logout`
- JWT authentication filter applied to all requests

### Utility Classes

#### SecurityUtil
Added static method:
- `getCurrentUserId()` - Extracts user ID from security context
- Throws UNAUTHORIZED error if not authenticated

#### CookieUtil
Added method:
- `generateAccessTokenCookie(String accessToken)` - Generates access token cookie for refresh endpoint

### Error Handling

#### New ErrorCodes
- `USERNAME_ALREADY_EXISTS` (409 CONFLICT)
- `EMAIL_ALREADY_EXISTS` (409 CONFLICT)
- `INVALID_REFRESH_TOKEN` (401 UNAUTHORIZED)
- `TOKEN_EXPIRED` (401 UNAUTHORIZED)
- `UNAUTHORIZED` (401 UNAUTHORIZED)

## Security Features

1. **Password Encryption:** BCrypt with default strength
2. **JWT Tokens:** 
   - Access token expiration configured in application.yml
   - Refresh token stored in Redis with TTL
3. **HTTP-Only Cookies:** Tokens stored in secure, HTTP-only cookies
4. **CORS Configuration:** Properly configured for dev/prod environments
5. **Input Validation:** Comprehensive validation on all request DTOs

## Token Flow

### Registration/Login Flow
1. User submits credentials
2. Server validates credentials
3. Server generates access token (JWT) and refresh token
4. Tokens stored in secure HTTP-only cookies
5. Refresh token saved to Redis with user ID as key
6. Response includes tokens in both body and cookies

### Logout Flow
1. User sends logout request with access token
2. Server extracts user ID from access token
3. Server deletes refresh token from Redis
4. Cookies cleared on client
5. Client must discard access token

### Token Refresh Flow
1. Client sends refresh token
2. Server validates refresh token against Redis
3. Server generates new access token
4. Response includes new access token

## Testing Considerations

### Unit Tests
- AuthService methods (register, login, logout, refresh)
- Validation logic for DTOs
- Error handling scenarios

### Integration Tests
- End-to-end flow for each endpoint
- Token generation and validation
- Redis token storage/retrieval
- Cookie handling

## Configuration

Required application.yml properties:
```yaml
jwt:
  accessTokenExpirationTime: 3600  # seconds
  refreshTokenExpirationTime: 604800  # seconds
  accessTokenSecret: <secret>
  refreshTokenSecret: <secret>
  issuer: ticket-online
```

## API Response Format

All endpoints follow the global response format:
```json
{
  "success": true,
  "data": { ... },
  "message": "Success"
}
```

Error responses:
```json
{
  "success": false,
  "message": "Error message",
  "errors": [
    {
      "field": "username",
      "message": "Username already exists"
    }
  ]
}
```

## Next Steps

1. Write comprehensive unit tests for AuthService
2. Create integration tests for all endpoints
3. Add rate limiting for auth endpoints (as per API spec)
4. Implement password strength validation
5. Add email verification flow (optional)
6. Add 2FA support (optional)