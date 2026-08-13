package com.ticket_online.domain.auth.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticket_online.domain.auth.fixture.TokenFixture;
import com.ticket_online.domain.user.domain.UserRole;
import com.ticket_online.global.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end integration tests for JWT authentication flow. Tests the real JwtAuthenticationFilter
 * and its interaction with Spring Security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationFlowTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;

    @Test
    @DisplayName("Should authenticate with Bearer token in Authorization header")
    void shouldAuthenticateWithBearerToken_InHeader() throws Exception {
        // Arrange
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;
        String accessToken = jwtUtil.generateAccessToken(userId, role);

        // Act & Then
        mockMvc.perform(
                        get("/api/v1/auth/test-endpoint")
                                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound()); // 404 means auth passed, endpoint doesn't exist
    }

    @Test
    @DisplayName("Should authenticate with access token in cookie")
    void shouldAuthenticateWithAccessToken_InCookie() throws Exception {
        // Arrange
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;
        String accessToken = jwtUtil.generateAccessToken(userId, role);
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);

        // Act & Then
        mockMvc.perform(get("/api/v1/auth/test-endpoint").cookie(accessTokenCookie))
                .andExpect(status().isNotFound()); // 404 means auth passed, endpoint doesn't exist
    }

    @Test
    @DisplayName("Should reissue tokens when access token expired but refresh token valid")
    void shouldReissueTokens_WhenAccessTokenExpiredButRefreshTokenValid() throws Exception {
        // Arrange
        Long userId = 1L;
        String expiredAccessToken = TokenFixture.createExpiredAccessToken(userId);
        String validRefreshToken = jwtUtil.generateRefreshToken(userId);

        Cookie expiredAccessCookie = new Cookie("accessToken", expiredAccessToken);
        Cookie validRefreshCookie = new Cookie("refreshToken", validRefreshToken);

        // Act & Then
        mockMvc.perform(
                        get("/api/v1/auth/test-endpoint")
                                .cookie(expiredAccessCookie, validRefreshCookie))
                .andExpect(status().isNotFound()) // 404 means auth passed, endpoint doesn't exist
                .andExpect(header().exists("Set-Cookie")); // New tokens should be set in cookies
    }

    @Test
    @DisplayName("Should return 401 when token signature is invalid")
    void shouldReturn401_WhenTokenSignatureIsInvalid() throws Exception {
        // Arrange
        String invalidToken = TokenFixture.createInvalidSignatureToken();

        // Act & Then
        mockMvc.perform(
                        get("/api/v1/auth/test-endpoint")
                                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }
}
