package com.ticket_online.domain.booking.dto;

public record HoldSeatResponse(String message, Long showId, int seatCount) {
    public static HoldSeatResponse success(Long showId, int seatCount) {
        return new HoldSeatResponse("Seats held successfully", showId, seatCount);
    }
}
