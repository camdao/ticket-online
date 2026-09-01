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
        List<SeatBookingResponse> seats) {

    public static BookingDetailResponse of(List<BookingDetailRow> details) {

        if (details.isEmpty()) {
            return null;
        }

        BookingDetailRow first = details.get(0);

        List<SeatBookingResponse> seats =
                details.stream()
                        .map(
                                detail ->
                                        SeatBookingResponse.from(
                                                detail.seatId(),
                                                detail.rowLabel(),
                                                detail.seatNumber(),
                                                detail.seatType(),
                                                detail.seatPrice()))
                        .toList();

        return new BookingDetailResponse(
                first.id(),
                first.bookingCode(),
                first.movieTitle(),
                first.movieImageUrl(),
                first.totalAmount(),
                first.status(),
                first.createdAt(),
                first.confirmedAt(),
                first.startTime(),
                first.endTime(),
                seats);
    }
}
