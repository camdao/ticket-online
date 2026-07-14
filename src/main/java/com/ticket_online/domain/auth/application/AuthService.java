package com.ticket_online.domain.auth.application;

import com.ticket_online.domain.auth.dao.RefreshTokenRepository;
import com.ticket_online.domain.auth.dto.request.RefreshTokenRequest;
import com.ticket_online.domain.auth.dto.request.RegisterRequest;
import com.ticket_online.domain.auth.dto.request.UsernamePasswordRequest;
import com.ticket_online.domain.auth.dto.response.AccessTokenResponse;
import com.ticket_online.domain.auth.dto.response.TokenPairResponse;
import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public TokenPairResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(request.password());

        // Create new user
        User user =
                User.createUser(
                        request.username(),
                        request.email(),
                        encodedPassword,
                        request.fullName(),
                        request.phoneNumber());

        // Save user
        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getUsername());

        // Generate tokens
        return getLoginResponse(savedUser);
    }

    public TokenPairResponse login(UsernamePasswordRequest request) {
        final User user =
                userRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validatePasswordMatches(user, request.password());

        log.info("User logged in successfully: {}", user.getUsername());

        return getLoginResponse(user);
    }

    public void logout(Long userId) {
        // Delete refresh token from Redis
        refreshTokenRepository.deleteById(userId);
        log.info("User logged out successfully: userId={}", userId);
    }

    public AccessTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        // Retrieve and validate refresh token
        var refreshTokenDto = jwtTokenService.retrieveRefreshToken(request.refreshToken());

        if (refreshTokenDto == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Get user to retrieve role
        User user =
                userRepository
                        .findById(refreshTokenDto.memberId())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Generate new access token
        String newAccessToken = jwtTokenService.createAccessToken(user.getId(), user.getRole());

        log.info("Access token refreshed for user: {}", user.getUsername());

        return AccessTokenResponse.from(newAccessToken, jwtProperties.accessTokenExpirationTime());
    }

    private TokenPairResponse getLoginResponse(User user) {
        String accessToken = jwtTokenService.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenService.createRefreshToken(user.getId());

        return TokenPairResponse.from(
                accessToken, refreshToken, jwtProperties.accessTokenExpirationTime());
    }

    private void validatePasswordMatches(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHES);
        }
    }
}
