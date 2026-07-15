package com.ticket_online.domain.bookings.dto.response;

import com.ticket_online.domain.bookings.domain.BookingStatus;
import java.math.BigDecimal;
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
public class BookingResponse {
    private Long id;
    private String bookingCode;
    private Long userId;
    private Long showtimeId;
    private String movieTitle;
    private String cinemaName;
    private String screenName;
    private LocalDateTime showtime;
    private List<SeatDto> seats;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
}
