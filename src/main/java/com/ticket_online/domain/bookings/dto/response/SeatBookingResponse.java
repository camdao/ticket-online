package com.ticket_online.domain.bookings.dto.response;

import com.ticket_online.domain.seats.domain.SeatType;
import java.math.BigDecimal;

public record SeatBookingResponse(
        Long id, String rowLabel, Integer seatNumber, SeatType seatType, BigDecimal price) {

    public static SeatBookingResponse from(
            Long id, String rowLabel, Integer seatNumber, SeatType seatType, BigDecimal price) {

        return new SeatBookingResponse(id, rowLabel, seatNumber, seatType, price);
    }
}
