package com.ticket_online.domain.bookings.dto.response;

import com.ticket_online.domain.seats.domain.SeatType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {
    private Long id;
    private String row;
    private Integer number;
    private SeatType type;
    private BigDecimal price;
}
