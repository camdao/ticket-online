package com.ticket_online.domain.catalog.dto.response;

import com.ticket_online.domain.catalog.domain.Seat;

public record SeatResponse(Long id, String seatCode, String status) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.getId(), seat.getSeatCode(), seat.getStatus().name());
    }
}
