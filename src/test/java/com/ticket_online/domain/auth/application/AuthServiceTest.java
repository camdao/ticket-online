package com.ticket_online.domain.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticket_online.domain.auth.dao.RefreshTokenRepository;
import com.ticket_online.domain.auth.dto.RefreshTokenDto;
import com.ticket_online.domain.auth.dto.request.RefreshTokenRequest;
import com.ticket_online.domain.auth.dto.request.RegisterRequest;
import com.ticket_online.domain.auth.dto.request.UsernamePasswordRequest;
import com.ticket_online.domain.auth.dto.response.AccessTokenResponse;
import com.ticket_online.domain.auth.dto.response.TokenPairResponse;
import com.ticket_online.domain.auth.fixture.AuthRequestFixture;
import com.ticket_online.domain.auth.fixture.UserFixture;
import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.domain.user.domain.UserRole;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.jwt.JwtProperties;
import com.ticket_online.global.util.SecurityUtil;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtProperties jwtProperties;
    @Mock private SecurityUtil securityUtil;

    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("Should register user successfully when valid request provided")
    void shouldRegisterUserSuccessfully_WhenValidRequest() {
        // Arrange
        RegisterRequest request = AuthRequestFixture.validRegisterRequest();
        User savedUser = UserFixture.createUserWithId(1L);
        String encodedPassword = "hashed-password";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        Long expirationTime = 3600L;

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenService.createAccessToken(savedUser.getId(), savedUser.getRole()))
                .thenReturn(accessToken);
        when(jwtTokenService.createRefreshToken(savedUser.getId())).thenReturn(refreshToken);
        when(jwtProperties.accessTokenExpirationTime()).thenReturn(expirationTime);

        // Act
        TokenPairResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(expirationTime);

        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenService).createAccessToken(savedUser.getId(), UserRole.ROLE_USER);
        verify(jwtTokenService).createRefreshToken(savedUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        RegisterRequest request = AuthRequestFixture.validRegisterRequest();
        User existingUser = UserFixture.createValidUser();

        when(userRepository.findByUsername(request.username()))
                .thenReturn(Optional.of(existingUser));

        // Act & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_ALREADY_EXISTS);

        verify(userRepository).findByUsername(request.username());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = AuthRequestFixture.validRegisterRequest();
        User existingUser = UserFixture.createValidUser();

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existingUser));

        // Act & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository).findByUsername(request.username());
        verify(userRepository).findByEmail(request.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully when credentials are valid")
    void shouldLoginSuccessfully_WhenCredentialsAreValid() {
        // Arrange
        UsernamePasswordRequest request = AuthRequestFixture.validLoginRequest();
        User user = UserFixture.createUserWithId(1L);
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        Long expirationTime = 3600L;

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtTokenService.createAccessToken(user.getId(), user.getRole()))
                .thenReturn(accessToken);
        when(jwtTokenService.createRefreshToken(user.getId())).thenReturn(refreshToken);
        when(jwtProperties.accessTokenExpirationTime()).thenReturn(expirationTime);

        // Act
        TokenPairResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(expirationTime);

        verify(userRepository).findByUsername(request.username());
        verify(passwordEncoder).matches(request.password(), user.getPassword());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowException_WhenUserNotFound() {
        // Arrange
        UsernamePasswordRequest request = AuthRequestFixture.validLoginRequest();

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());

        // Act & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findByUsername(request.username());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password does not match")
    void shouldThrowException_WhenPasswordDoesNotMatch() {
        // Arrange
        UsernamePasswordRequest request = AuthRequestFixture.validLoginRequest();
        User user = UserFixture.createUserWithId(1L);

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        // Act & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_NOT_MATCHES);

        verify(userRepository).findByUsername(request.username());
        verify(passwordEncoder).matches(request.password(), user.getPassword());
    }

    @Test
    @DisplayName("Should logout successfully when user is authenticated")
    void shouldLogoutSuccessfully_WhenUserIsAuthenticated() {
        // Arrange
        Long userId = 1L;

        when(securityUtil.getCurrentUserId()).thenReturn(userId);

        // Act
        authService.logout();

        // Then
        verify(securityUtil).getCurrentUserId();
        verify(refreshTokenRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should refresh access token when refresh token is valid")
    void shouldRefreshAccessToken_WhenRefreshTokenIsValid() {
        // Arrange
        String refreshTokenValue = "valid-refresh-token";
        RefreshTokenRequest request =
                AuthRequestFixture.validRefreshTokenRequest(refreshTokenValue);
        Long userId = 1L;
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(userId, refreshTokenValue, 604800L);
        User user = UserFixture.createUserWithIdAndRole(userId, UserRole.ROLE_USER);
        String newAccessToken = "new-access-token";
        Long expirationTime = 3600L;

        when(jwtTokenService.retrieveRefreshToken(refreshTokenValue)).thenReturn(refreshTokenDto);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtTokenService.createAccessToken(user.getId(), user.getRole()))
                .thenReturn(newAccessToken);
        when(jwtProperties.accessTokenExpirationTime()).thenReturn(expirationTime);

        // Act
        AccessTokenResponse response = authService.refreshAccessToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(newAccessToken);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(expirationTime);

        verify(jwtTokenService).retrieveRefreshToken(refreshTokenValue);
        verify(userRepository).findById(userId);
        verify(jwtTokenService).createAccessToken(user.getId(), user.getRole());
    }

    @Test
    @DisplayName("Should throw exception when refresh token is invalid")
    void shouldThrowException_WhenRefreshTokenIsInvalid() {
        // Arrange
        String refreshTokenValue = "invalid-refresh-token";
        RefreshTokenRequest request =
                AuthRequestFixture.validRefreshTokenRequest(refreshTokenValue);

        when(jwtTokenService.retrieveRefreshToken(refreshTokenValue)).thenReturn(null);

        // Act & Then
        assertThatThrownBy(() -> authService.refreshAccessToken(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);

        verify(jwtTokenService).retrieveRefreshToken(refreshTokenValue);
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found during refresh")
    void shouldThrowException_WhenUserNotFoundDuringRefresh() {
        // Arrange
        String refreshTokenValue = "valid-refresh-token";
        RefreshTokenRequest request =
                AuthRequestFixture.validRefreshTokenRequest(refreshTokenValue);
        Long userId = 1L;
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(userId, refreshTokenValue, 604800L);

        when(jwtTokenService.retrieveRefreshToken(refreshTokenValue)).thenReturn(refreshTokenDto);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Then
        assertThatThrownBy(() -> authService.refreshAccessToken(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(jwtTokenService).retrieveRefreshToken(refreshTokenValue);
        verify(userRepository).findById(userId);
    }
}
