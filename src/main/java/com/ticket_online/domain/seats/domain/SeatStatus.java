package com.ticket_online.domain.seats.domain;

/** Enum representing the status of a seat for a specific showtime */
public enum SeatStatus {
    /** Seat is available for booking */
    AVAILABLE,

    /** Seat is temporarily held by a user (not yet confirmed) */
    HELD,

    /** Seat is booked and confirmed */
    BOOKED
}
