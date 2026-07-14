package com.ticket_online.domain.seats.dto.response;

import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.seats.domain.SeatStatus;
import com.ticket_online.domain.seats.domain.SeatType;
import lombok.Builder;
import lombok.Getter;

/** Response DTO for seat information */
@Getter
@Builder
public class SeatResponse {

    private Long id;
    private String row;
    private Integer number;
    private SeatType type;
    private Long price;
    private SeatStatus status;

    /** Create SeatResponse from Seat entity */
    public static SeatResponse from(Seat seat, SeatStatus status) {
        return SeatResponse.builder()
                .id(seat.getId())
                .row(seat.getRow())
                .number(seat.getNumber())
                .type(seat.getType())
                .price(seat.getBasePrice())
                .status(status)
                .build();
    }

    /** Create SeatResponse with AVAILABLE status */
    public static SeatResponse from(Seat seat) {
        return from(seat, SeatStatus.AVAILABLE);
    }
}
