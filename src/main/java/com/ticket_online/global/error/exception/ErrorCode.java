package com.ticket_online.global.error.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Example
    SAMPLE_ERROR(HttpStatus.BAD_REQUEST, "Sample Error Message"),

    // Common
    METHOD_ARGUMENT_TYPE_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "The requested value type is incorrect, causing binding failure."),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "The HTTP method is not supported."),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR, "Server error, please contact the administrator."),

    // HoldSeat
    TOO_MANY_SEATS(HttpStatus.BAD_REQUEST, "You cannot hold more than 4 seats at once."),

    SEAT_ALREADY_SOLD(HttpStatus.BAD_REQUEST, "The seat is already sold."),

    SEAT_ALREADY_HELD(HttpStatus.BAD_REQUEST, "The seat is already held by another user."),

    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found."),
    SEAT_HOLD_FAILED(HttpStatus.BAD_REQUEST, "Failed to hold the seat. Please try again."),
    SEAT_ALREADY_EXISTS(HttpStatus.CONFLICT, "Seat already exists at this position."),
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "Seat is not available."),
    SEATS_NOT_FOUND(HttpStatus.NOT_FOUND, "One or more seats not found."),

    // Order
    ORDER_SEAT_HOLD_FAILED(
            HttpStatus.BAD_REQUEST,
            "False hold result from Redis. Failed to hold the seat. Please try again."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested order was not found."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested member was not found."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Username already exists."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email already exists."),

    // Security
    PASSWORD_NOT_MATCHES(HttpStatus.BAD_REQUEST, "The provided password does not match."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication is required."),

    // Show
    SHOW_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested show was not found."),

    // Cinema
    CINEMA_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested cinema was not found."),

    // Screen
    SCREEN_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested screen was not found."),

    // Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested room was not found."),

    // Movie
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested movie was not found."),

    // Showtime
    SHOWTIME_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested showtime was not found.");

    private final HttpStatus status;
    private final String message;
}
