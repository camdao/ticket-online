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

    // Order
    ORDER_SEAT_HOLD_FAILED(
            HttpStatus.BAD_REQUEST, "Failed to hold the seat for the order. Please try again."),

    // User
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested member was not found."),

    // Security
    PASSWORD_NOT_MATCHES(HttpStatus.BAD_REQUEST, "The provided password does not match."),
    ;

    private final HttpStatus status;
    private final String message;
}
