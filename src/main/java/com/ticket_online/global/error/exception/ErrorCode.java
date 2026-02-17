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

    SEAT_NotFound(HttpStatus.NOT_FOUND, "The requested resource was not found.");

    private final HttpStatus status;
    private final String message;
}
