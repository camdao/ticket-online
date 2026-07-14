package com.ticket_online.domain.auth.api;

import com.ticket_online.domain.auth.application.AuthService;
import com.ticket_online.domain.auth.dto.request.RefreshTokenRequest;
import com.ticket_online.domain.auth.dto.request.RegisterRequest;
import com.ticket_online.domain.auth.dto.request.UsernamePasswordRequest;
import com.ticket_online.domain.auth.dto.response.AccessTokenResponse;
import com.ticket_online.domain.auth.dto.response.TokenPairResponse;
import com.ticket_online.global.util.CookieUtil;
import com.ticket_online.global.util.SecurityUtil;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<TokenPairResponse> register(@Valid @RequestBody RegisterRequest request) {
        TokenPairResponse response = authService.register(request);

        String accessToken = response.accessToken();
        String refreshToken = response.refreshToken();
        HttpHeaders tokenHeaders = cookieUtil.generateTokenCookies(accessToken, refreshToken);

        return ResponseEntity.status(HttpStatus.CREATED).headers(tokenHeaders).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(
            @Valid @RequestBody UsernamePasswordRequest request) {
        TokenPairResponse response = authService.login(request);

        String accessToken = response.accessToken();
        String refreshToken = response.refreshToken();
        HttpHeaders tokenHeaders = cookieUtil.generateTokenCookies(accessToken, refreshToken);

        return ResponseEntity.ok().headers(tokenHeaders).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        authService.logout(userId);

        HttpHeaders clearCookieHeaders = cookieUtil.clearTokenCookies();

        return ResponseEntity.ok()
                .headers(clearCookieHeaders)
                .body(Map.of("message", "Logout successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AccessTokenResponse response = authService.refreshAccessToken(request);

        String accessToken = response.accessToken();
        HttpHeaders accessTokenHeader = cookieUtil.generateAccessTokenCookie(accessToken);

        return ResponseEntity.ok().headers(accessTokenHeader).body(response);
    }
}
