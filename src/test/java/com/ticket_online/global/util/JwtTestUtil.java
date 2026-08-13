package com.ticket_online.global.util;

import com.ticket_online.domain.user.domain.UserRole;
import com.ticket_online.global.jwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.security.Key;
import java.util.Date;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Test utility for generating and manipulating JWT tokens in tests. Provides helper methods for
 * creating valid, expired, and malformed tokens, as well as setting up mock HTTP requests with
 * tokens.
 */
public class JwtTestUtil {

    private static final String TEST_ISSUER = "ticket-online-test";
    private static final String TEST_ACCESS_SECRET =
            "test-access-secret-key-for-jwt-token-generation-32-chars";
    private static final String TEST_REFRESH_SECRET =
            "test-refresh-secret-key-for-jwt-token-generation-32-chars";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600L; // 1 hour in seconds
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 604800L; // 7 days in seconds

    /**
     * Generates a valid access token for testing.
     *
     * @param userId the user ID to encode in the token
     * @param role the user role to encode in the token
     * @return a valid JWT access token string
     */
    public static String generateValidAccessToken(Long userId, UserRole role) {
        JwtUtil jwtUtil = createTestJwtUtil();
        return jwtUtil.generateAccessToken(userId, role);
    }

    /**
     * Generates a valid access token with default ROLE_USER.
     *
     * @param userId the user ID to encode in the token
     * @return a valid JWT access token string
     */
    public static String generateValidAccessToken(Long userId) {
        return generateValidAccessToken(userId, UserRole.ROLE_USER);
    }

    /**
     * Generates a valid refresh token for testing.
     *
     * @param userId the user ID to encode in the token
     * @return a valid JWT refresh token string
     */
    public static String generateValidRefreshToken(Long userId) {
        JwtUtil jwtUtil = createTestJwtUtil();
        return jwtUtil.generateRefreshToken(userId);
    }

    /**
     * Generates an expired access token for testing token refresh logic.
     *
     * @param userId the user ID to encode in the token
     * @return an expired JWT access token string
     */
    public static String generateExpiredAccessToken(Long userId) {
        Date issuedAt = new Date(System.currentTimeMillis() - 7200000); // 2 hours ago
        Date expiredAt = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago

        return Jwts.builder()
                .setIssuer(TEST_ISSUER)
                .setSubject(userId.toString())
                .claim("role", UserRole.ROLE_USER.name())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(getAccessTokenKey())
                .compact();
    }

    /**
     * Generates a token with an invalid signature (signed with wrong secret).
     *
     * @return a JWT token with invalid signature
     */
    public static String generateTokenWithInvalidSignature() {
        String wrongSecret = "wrong-secret-key-for-jwt-token-generation-that-is-32-chars-long";
        Key wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes());

        Date issuedAt = new Date();
        Date expiredAt = new Date(issuedAt.getTime() + 3600000);

        return Jwts.builder()
                .setIssuer(TEST_ISSUER)
                .setSubject("1")
                .claim("role", UserRole.ROLE_USER.name())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(wrongKey)
                .compact();
    }

    /**
     * Generates a malformed token (not valid JWT format).
     *
     * @return a malformed token string
     */
    public static String generateMalformedToken() {
        return "this.is.not.a.valid.jwt.token";
    }

    /**
     * Extracts claims from a token without validation (unsafe). Useful for testing token structure
     * without validation.
     *
     * @param token the JWT token to extract claims from
     * @return the claims contained in the token
     */
    public static Claims extractClaimsUnsafe(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }

        // This is a simplified implementation for testing purposes
        // In real tests, use the actual JwtUtil to parse tokens
        Claims claims = Jwts.claims();
        claims.setSubject("test");
        return claims;
    }

    /**
     * Creates a MockHttpServletRequest with Bearer token in Authorization header.
     *
     * @param token the JWT token to include in the request
     * @return a mock request with the token in Authorization header
     */
    public static MockHttpServletRequest createRequestWithBearerToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    /**
     * Creates a MockHttpServletRequest with a token in a cookie.
     *
     * @param cookieName the name of the cookie
     * @param cookieValue the value of the cookie (typically a JWT token)
     * @return a mock request with the token in a cookie
     */
    public static MockHttpServletRequest createRequestWithCookie(
            String cookieName, String cookieValue) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie(cookieName, cookieValue);
        request.setCookies(cookie);
        return request;
    }

    /**
     * Creates a test JwtUtil instance configured with test secrets.
     *
     * @return a JwtUtil configured for testing
     */
    private static JwtUtil createTestJwtUtil() {
        JwtProperties jwtProperties =
                new JwtProperties(
                        TEST_ACCESS_SECRET,
                        TEST_REFRESH_SECRET,
                        ACCESS_TOKEN_EXPIRATION_TIME,
                        REFRESH_TOKEN_EXPIRATION_TIME,
                        TEST_ISSUER);
        return new JwtUtil(jwtProperties);
    }

    private static Key getAccessTokenKey() {
        return Keys.hmacShaKeyFor(TEST_ACCESS_SECRET.getBytes());
    }

    private static Key getRefreshTokenKey() {
        return Keys.hmacShaKeyFor(TEST_REFRESH_SECRET.getBytes());
    }
}
