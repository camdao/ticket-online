package com.ticket_online.domain.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticket_online.domain.auth.dao.RefreshTokenRepository;
import com.ticket_online.domain.auth.domain.RefreshToken;
import com.ticket_online.domain.auth.dto.AccessTokenDto;
import com.ticket_online.domain.auth.dto.RefreshTokenDto;
import com.ticket_online.domain.user.domain.UserRole;
import com.ticket_online.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private JwtTokenService jwtTokenService;

    @Test
    @DisplayName("Should create access token with valid parameters")
    void shouldCreateAccessToken_WithValidParameters() {
        // Arrange
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;
        String expectedToken = "generated-access-token";

        when(jwtUtil.generateAccessToken(userId, role)).thenReturn(expectedToken);

        // Act
        String result = jwtTokenService.createAccessToken(userId, role);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedToken);
        verify(jwtUtil).generateAccessToken(userId, role);
    }

    @Test
    @DisplayName("Should create refresh token and save to Redis")
    void shouldCreateRefreshToken_AndSaveToRedis() {
        // Arrange
        Long userId = 1L;
        String expectedToken = "generated-refresh-token";
        Long ttl = 604800L;

        when(jwtUtil.generateRefreshToken(userId)).thenReturn(expectedToken);
        when(jwtUtil.getRefreshTokenExpirationTime()).thenReturn(ttl);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = jwtTokenService.createRefreshToken(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedToken);
        verify(jwtUtil).generateRefreshToken(userId);
        verify(jwtUtil).getRefreshTokenExpirationTime();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should retrieve access token when valid")
    void shouldRetrieveAccessToken_WhenValid() {
        // Arrange
        String tokenValue = "valid-access-token";
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;
        AccessTokenDto expectedDto = new AccessTokenDto(userId, role, tokenValue);

        when(jwtUtil.parseAccessToken(tokenValue)).thenReturn(expectedDto);

        // Act
        AccessTokenDto result = jwtTokenService.retrieveAccessToken(tokenValue);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.memberId()).isEqualTo(userId);
        assertThat(result.userRole()).isEqualTo(role);
        assertThat(result.tokenValue()).isEqualTo(tokenValue);
        verify(jwtUtil).parseAccessToken(tokenValue);
    }

    @Test
    @DisplayName("Should retrieve refresh token when valid and exists in Redis")
    void shouldRetrieveRefreshToken_WhenValidAndExistsInRedis() {
        // Arrange
        String tokenValue = "valid-refresh-token";
        Long userId = 1L;
        Long ttl = 604800L;
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(userId, tokenValue, ttl);
        RefreshToken storedToken =
                RefreshToken.builder().memberId(userId).token(tokenValue).ttl(ttl).build();

        when(jwtUtil.parseRefreshToken(tokenValue)).thenReturn(refreshTokenDto);
        when(refreshTokenRepository.findById(userId)).thenReturn(Optional.of(storedToken));

        // Act
        RefreshTokenDto result = jwtTokenService.retrieveRefreshToken(tokenValue);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.memberId()).isEqualTo(userId);
        assertThat(result.tokenValue()).isEqualTo(tokenValue);
        verify(jwtUtil).parseRefreshToken(tokenValue);
        verify(refreshTokenRepository).findById(userId);
    }

    @Test
    @DisplayName("Should return null when refresh token not in Redis")
    void shouldReturnNull_WhenRefreshTokenNotInRedis() {
        // Arrange
        String tokenValue = "valid-refresh-token";
        Long userId = 1L;
        Long ttl = 604800L;
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(userId, tokenValue, ttl);

        when(jwtUtil.parseRefreshToken(tokenValue)).thenReturn(refreshTokenDto);
        when(refreshTokenRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        RefreshTokenDto result = jwtTokenService.retrieveRefreshToken(tokenValue);

        // Then
        assertThat(result).isNull();
        verify(jwtUtil).parseRefreshToken(tokenValue);
        verify(refreshTokenRepository).findById(userId);
    }

    @Test
    @DisplayName("Should reissue access token when expired")
    void shouldReissueAccessToken_WhenExpired() {
        // Arrange
        String expiredToken = "expired-access-token";
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;

        // Create ExpiredJwtException with claims
        Claims claims = Jwts.claims();
        claims.setSubject(userId.toString());
        claims.put("role", role.name());

        ExpiredJwtException exception = new ExpiredJwtException(null, claims, "Token expired");

        AccessTokenDto newTokenDto = new AccessTokenDto(userId, role, "new-access-token");

        when(jwtUtil.parseAccessToken(expiredToken)).thenThrow(exception);
        when(jwtUtil.generateAccessTokenDto(userId, role)).thenReturn(newTokenDto);

        // Act
        AccessTokenDto result = jwtTokenService.reissueAccessTokenIfExpired(expiredToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.memberId()).isEqualTo(userId);
        assertThat(result.userRole()).isEqualTo(role);
        verify(jwtUtil).parseAccessToken(expiredToken);
        verify(jwtUtil).generateAccessTokenDto(userId, role);
    }
}
