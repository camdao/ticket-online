package com.ticket_online.domain.bookings.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatsResponse {
    private String holdToken;
    private Long showtimeId;
    private List<Long> seatIds;
    private LocalDateTime expiresAt;
    private Integer remainingSeconds;
}