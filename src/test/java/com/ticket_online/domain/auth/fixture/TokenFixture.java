package com.ticket_online.domain.auth.fixture;

import com.ticket_online.domain.user.domain.UserRole;
import com.ticket_online.global.jwt.JwtProperties;
import com.ticket_online.global.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

/**
 * Test fixture for creating JWT tokens with various configurations. Uses real JwtUtil to generate
 * realistic tokens for testing.
 */
public class TokenFixture {

    private static final String TEST_ISSUER = "ticket-online-test";
    private static final String TEST_ACCESS_SECRET =
            "test-access-secret-key-for-jwt-token-generation-32-chars";
    private static final String TEST_REFRESH_SECRET =
            "test-refresh-secret-key-for-jwt-token-generation-32-chars";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600L; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 604800L; // 7 days

    /** Creates a JwtUtil instance configured for testing. */
    public static JwtUtil createTestJwtUtil() {
        JwtProperties jwtProperties =
                new JwtProperties(
                        TEST_ACCESS_SECRET,
                        TEST_REFRESH_SECRET,
                        ACCESS_TOKEN_EXPIRATION_TIME,
                        REFRESH_TOKEN_EXPIRATION_TIME,
                        TEST_ISSUER);
        return new JwtUtil(jwtProperties);
    }

    /** Creates a valid access token for testing. */
    public static String createValidAccessToken(Long userId, UserRole role) {
        JwtUtil jwtUtil = createTestJwtUtil();
        return jwtUtil.generateAccessToken(userId, role);
    }

    /** Creates a valid access token with default ROLE_USER. */
    public static String createValidAccessToken(Long userId) {
        return createValidAccessToken(userId, UserRole.ROLE_USER);
    }

    /** Creates an expired access token for testing refresh logic. */
    public static String createExpiredAccessToken(Long userId) {
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

    /** Creates a token with invalid signature (wrong secret). */
    public static String createInvalidSignatureToken() {
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

    /** Creates a malformed token (not valid JWT format). */
    public static String createMalformedToken() {
        return "this.is.not.a.valid.jwt.token";
    }

    /** Creates a valid refresh token for testing. */
    public static String createRefreshToken(Long userId) {
        JwtUtil jwtUtil = createTestJwtUtil();
        return jwtUtil.generateRefreshToken(userId);
    }

    /** Creates an expired refresh token. */
    public static String createExpiredRefreshToken(Long userId) {
        Date issuedAt = new Date(System.currentTimeMillis() - 1209600000); // 14 days ago
        Date expiredAt = new Date(System.currentTimeMillis() - 604800000); // 7 days ago

        return Jwts.builder()
                .setIssuer(TEST_ISSUER)
                .setSubject(userId.toString())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(getRefreshTokenKey())
                .compact();
    }

    private static Key getAccessTokenKey() {
        return Keys.hmacShaKeyFor(TEST_ACCESS_SECRET.getBytes());
    }

    private static Key getRefreshTokenKey() {
        return Keys.hmacShaKeyFor(TEST_REFRESH_SECRET.getBytes());
    }
}
