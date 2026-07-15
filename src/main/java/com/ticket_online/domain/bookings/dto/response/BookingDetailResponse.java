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
public class BookingDetailResponse {
    private Long id;
    private String bookingCode;
    private Long userId;
    private MovieInfo movie;
    private CinemaInfo cinema;
    private ScreenInfo screen;
    private LocalDateTime showtime;
    private List<SeatDto> seats;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime expiresAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieInfo {
        private Long id;
        private String title;
        private String posterUrl;
        private Integer duration;
        private String ageRating;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CinemaInfo {
        private Long id;
        private String name;
        private String address;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreenInfo {
        private Long id;
        private String name;
        private String type;
    }
}