package com.ticket_online.domain.bookings.dao;

import com.ticket_online.domain.bookings.domain.BookingStatus;
import com.ticket_online.domain.seats.domain.SeatType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BookingDetailProjection {

    Long getBookingId();

    String getBookingCode();

    String getMovieTitle();

    String getMovieImageUrl();

    BigDecimal getTotalAmount();

    BookingStatus getStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getConfirmedAt();

    LocalDateTime getStartTime();

    LocalDateTime getEndTime();

    Long getSeatId();

    String getSeatRow();

    Integer getSeatNumber();

    SeatType getSeatType();

    BigDecimal getPrice();
}
