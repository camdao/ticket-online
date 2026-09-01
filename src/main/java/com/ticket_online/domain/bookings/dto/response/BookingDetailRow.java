package com.ticket_online.domain.bookings.dto.response;

import com.ticket_online.domain.bookings.domain.BookingStatus;
import com.ticket_online.domain.seats.domain.SeatType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingDetailRow(
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
        Long seatId,
        String rowLabel,
        Integer seatNumber,
        SeatType seatType,
        BigDecimal seatPrice) {}
