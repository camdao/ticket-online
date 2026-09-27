package com.ticket_online.domain.bookings.dto.response;

import com.ticket_online.domain.bookings.domain.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingDetailResponse(
        Long id,
        String bookingCode,
        String movieTitle,
        String movieImageUrl,
        BigDecimal totalAmount,
        BookingStatus status,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<SeatBookingResponse> seats) {}
