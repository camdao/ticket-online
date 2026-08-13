package com.ticket_online.domain.auth.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket_online.domain.auth.application.AuthService;
import com.ticket_online.domain.auth.dto.request.RefreshTokenRequest;
import com.ticket_online.domain.auth.dto.request.RegisterRequest;
import com.ticket_online.domain.auth.dto.request.UsernamePasswordRequest;
import com.ticket_online.domain.auth.dto.response.AccessTokenResponse;
import com.ticket_online.domain.auth.dto.response.TokenPairResponse;
import com.ticket_online.domain.auth.fixture.AuthRequestFixture;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.util.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AuthService authService;
    @MockBean private CookieUtil cookieUtil;
    @Autowired private ObjectMapper objectMapper;

    // ========== POST /api/v1/auth/register Tests ==========

    @Test
    @DisplayName("Should register successfully with valid request and return 201 Created")
    void shouldRegisterSuccessfully_WithValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        RegisterRequest request = AuthRequestFixture.validRegisterRequest();
        TokenPairResponse response = TokenPairResponse.from("access-token", "refresh-token", 3600L);
        HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.add("Set-Cookie", "accessToken=access-token; HttpOnly; Secure");
        cookieHeaders.add("Set-Cookie", "refreshToken=refresh-token; HttpOnly; Secure");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);
        when(cookieUtil.generateTokenCookies(response.accessToken(), response.refreshToken()))
                .thenReturn(cookieHeaders);

        // Act & Then
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(
                        result -> {
                            System.out.println("=== Register Test Response ===");
                            System.out.println("Status: " + result.getResponse().getStatus());
                            System.out.println(
                                    "Content: " + result.getResponse().getContentAsString());
                            System.out.println("Headers: " + result.getResponse().getHeaderNames());
                        })
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when username is too short")
    void shouldReturn400_WhenUsernameIsTooShort() throws Exception {
        // Arrange
        RegisterRequest request = AuthRequestFixture.registerRequestWithShortUsername();

        // Act & Then
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 Conflict when username already exists")
    void shouldReturn409_WhenUsernameAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest request = AuthRequestFixture.validRegisterRequest();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS));

        // Act & Then
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ========== POST /api/v1/auth/login Tests ==========

    @Test
    @DisplayName("Should login successfully with valid credentials and return 200 OK")
    void shouldLoginSuccessfully_WithValidCredentials_ReturnsOk() throws Exception {
        // Arrange
        UsernamePasswordRequest request = AuthRequestFixture.validLoginRequest();
        TokenPairResponse response = TokenPairResponse.from("access-token", "refresh-token", 3600L);
        HttpHeaders cookieHeaders = new HttpHeaders();
        cookieHeaders.add("Set-Cookie", "accessToken=access-token; HttpOnly; Secure");
        cookieHeaders.add("Set-Cookie", "refreshToken=refresh-token; HttpOnly; Secure");

        when(authService.login(any(UsernamePasswordRequest.class))).thenReturn(response);
        when(cookieUtil.generateTokenCookies(response.accessToken(), response.refreshToken()))
                .thenReturn(cookieHeaders);

        // Act & Then
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when password is incorrect")
    void shouldReturn401_WhenPasswordIsIncorrect() throws Exception {
        // Arrange
        UsernamePasswordRequest request = AuthRequestFixture.validLoginRequest();

        when(authService.login(any(UsernamePasswordRequest.class)))
                .thenThrow(new CustomException(ErrorCode.PASSWORD_NOT_MATCHES));

        // Act & Then
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ========== POST /api/v1/auth/logout Tests ==========

    @Test
    @DisplayName("Should logout successfully when authenticated and return 200 OK")
    void shouldLogoutSuccessfully_WhenAuthenticated_ReturnsOk() throws Exception {
        // Arrange
        HttpHeaders clearHeaders = new HttpHeaders();
        clearHeaders.add("Set-Cookie", "accessToken=; Max-Age=0; HttpOnly; Secure");
        clearHeaders.add("Set-Cookie", "refreshToken=; Max-Age=0; HttpOnly; Secure");

        doNothing().when(authService).logout();
        when(cookieUtil.clearTokenCookies()).thenReturn(clearHeaders);

        // Act & Then
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(header().exists("Set-Cookie"));
    }

    // ========== POST /api/v1/auth/refresh Tests ==========

    @Test
    @DisplayName("Should refresh access token with valid refresh token and return 200 OK")
    void shouldRefreshAccessToken_WithValidRefreshToken_ReturnsOk() throws Exception {
        // Arrange
        RefreshTokenRequest request =
                AuthRequestFixture.validRefreshTokenRequest("valid-refresh-token");
        AccessTokenResponse response = AccessTokenResponse.from("new-access-token", 3600L);
        HttpHeaders accessTokenHeader = new HttpHeaders();
        accessTokenHeader.add("Set-Cookie", "accessToken=new-access-token; HttpOnly; Secure");

        when(authService.refreshAccessToken(any(RefreshTokenRequest.class))).thenReturn(response);
        when(cookieUtil.generateAccessTokenCookie(response.accessToken()))
                .thenReturn(accessTokenHeader);

        // Act & Then
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when refresh token is invalid")
    void shouldReturn401_WhenRefreshTokenIsInvalid() throws Exception {
        // Arrange
        RefreshTokenRequest request =
                AuthRequestFixture.validRefreshTokenRequest("invalid-refresh-token");

        when(authService.refreshAccessToken(any(RefreshTokenRequest.class)))
                .thenThrow(new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        // Act & Then
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
