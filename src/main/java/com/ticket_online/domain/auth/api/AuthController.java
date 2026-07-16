package com.ticket_online.domain.auth.api;

import com.ticket_online.domain.auth.application.AuthService;
import com.ticket_online.domain.auth.dto.request.RefreshTokenRequest;
import com.ticket_online.domain.auth.dto.request.RegisterRequest;
import com.ticket_online.domain.auth.dto.request.UsernamePasswordRequest;
import com.ticket_online.domain.auth.dto.response.AccessTokenResponse;
import com.ticket_online.domain.auth.dto.response.LogoutResponse;
import com.ticket_online.domain.auth.dto.response.TokenPairResponse;
import com.ticket_online.global.util.CookieUtil;
import com.ticket_online.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Operation(
            summary = "Register a new user",
            description =
                    "Creates a new user account and returns access and refresh tokens. Tokens are also"
                            + " set as HTTP-only cookies.")
    @PostMapping("/register")
    public ResponseEntity<TokenPairResponse> register(@Valid @RequestBody RegisterRequest request) {
        TokenPairResponse response = authService.register(request);

        String accessToken = response.accessToken();
        String refreshToken = response.refreshToken();
        HttpHeaders tokenHeaders = cookieUtil.generateTokenCookies(accessToken, refreshToken);

        return ResponseEntity.status(HttpStatus.CREATED).headers(tokenHeaders).body(response);
    }

    @Operation(
            summary = "User login",
            description =
                    "Authenticates user credentials and returns access and refresh tokens. Tokens are"
                            + " also set as HTTP-only cookies.")
    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(
            @Valid @RequestBody UsernamePasswordRequest request) {
        TokenPairResponse response = authService.login(request);

        String accessToken = response.accessToken();
        String refreshToken = response.refreshToken();
        HttpHeaders tokenHeaders = cookieUtil.generateTokenCookies(accessToken, refreshToken);

        return ResponseEntity.ok().headers(tokenHeaders).body(response);
    }

    @Operation(
            summary = "User logout",
            description =
                    "Logs out the current user by invalidating their refresh token and clearing token"
                            + " cookies. Requires valid JWT authentication.")
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout() {
        authService.logout();

        HttpHeaders clearCookieHeaders = cookieUtil.clearTokenCookies();

        return ResponseEntity.ok()
                .headers(clearCookieHeaders)
                .body(LogoutResponse.success());
    }

    @Operation(
            summary = "Refresh access token",
            description =
                    "Generates a new access token using a valid refresh token. The new access token is"
                            + " returned and also set as an HTTP-only cookie.")
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AccessTokenResponse response = authService.refreshAccessToken(request);

        String accessToken = response.accessToken();
        HttpHeaders accessTokenHeader = cookieUtil.generateAccessTokenCookie(accessToken);

        return ResponseEntity.ok().headers(accessTokenHeader).body(response);
    }
}
