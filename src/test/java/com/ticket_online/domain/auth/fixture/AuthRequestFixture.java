package com.ticket_online.domain.auth.fixture;

import com.ticket_online.domain.auth.dto.request.RefreshTokenRequest;
import com.ticket_online.domain.auth.dto.request.RegisterRequest;
import com.ticket_online.domain.auth.dto.request.UsernamePasswordRequest;

/**
 * Test fixture for creating Auth request DTOs with various configurations. Provides valid and
 * invalid requests for testing validation and business logic.
 */
public class AuthRequestFixture {

    private static final String VALID_USERNAME = "johndoe";
    private static final String VALID_EMAIL = "john.doe@example.com";
    private static final String VALID_PASSWORD = "SecurePass123";
    private static final String VALID_FULL_NAME = "John Doe";
    private static final String VALID_PHONE = "0123456789";

    /** Creates a valid RegisterRequest with all fields properly set. */
    public static RegisterRequest validRegisterRequest() {
        return new RegisterRequest(
                VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, VALID_FULL_NAME, VALID_PHONE);
    }

    /** Creates a RegisterRequest with invalid email format. */
    public static RegisterRequest registerRequestWithInvalidEmail() {
        return new RegisterRequest(
                VALID_USERNAME, "invalid-email", VALID_PASSWORD, VALID_FULL_NAME, VALID_PHONE);
    }

    /** Creates a RegisterRequest with password shorter than 8 characters. */
    public static RegisterRequest registerRequestWithShortPassword() {
        return new RegisterRequest(
                VALID_USERNAME, VALID_EMAIL, "short", VALID_FULL_NAME, VALID_PHONE);
    }

    /**
     * Creates a RegisterRequest with invalid phone number pattern. Valid pattern is 10 digits
     * starting with 0.
     */
    public static RegisterRequest registerRequestWithInvalidPhone() {
        return new RegisterRequest(
                VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, VALID_FULL_NAME, "123456789");
    }

    /** Creates a RegisterRequest with username shorter than 3 characters. */
    public static RegisterRequest registerRequestWithShortUsername() {
        return new RegisterRequest("ab", VALID_EMAIL, VALID_PASSWORD, VALID_FULL_NAME, VALID_PHONE);
    }

    /** Creates a RegisterRequest with custom username. */
    public static RegisterRequest registerRequestWithUsername(String username) {
        return new RegisterRequest(
                username, VALID_EMAIL, VALID_PASSWORD, VALID_FULL_NAME, VALID_PHONE);
    }

    /** Creates a RegisterRequest with custom email. */
    public static RegisterRequest registerRequestWithEmail(String email) {
        return new RegisterRequest(
                VALID_USERNAME, email, VALID_PASSWORD, VALID_FULL_NAME, VALID_PHONE);
    }

    /** Creates a valid UsernamePasswordRequest for login. */
    public static UsernamePasswordRequest validLoginRequest() {
        return new UsernamePasswordRequest(VALID_USERNAME, VALID_PASSWORD);
    }

    /** Creates a UsernamePasswordRequest with custom username. */
    public static UsernamePasswordRequest loginRequestWithUsername(String username) {
        return new UsernamePasswordRequest(username, VALID_PASSWORD);
    }

    /** Creates a UsernamePasswordRequest with custom password. */
    public static UsernamePasswordRequest loginRequestWithPassword(String password) {
        return new UsernamePasswordRequest(VALID_USERNAME, password);
    }

    /** Creates a UsernamePasswordRequest with custom credentials. */
    public static UsernamePasswordRequest loginRequest(String username, String password) {
        return new UsernamePasswordRequest(username, password);
    }

    /** Creates a valid RefreshTokenRequest. */
    public static RefreshTokenRequest validRefreshTokenRequest(String token) {
        return new RefreshTokenRequest(token);
    }

    /** Creates a RefreshTokenRequest with empty token. */
    public static RefreshTokenRequest refreshTokenRequestWithEmptyToken() {
        return new RefreshTokenRequest("");
    }
}
