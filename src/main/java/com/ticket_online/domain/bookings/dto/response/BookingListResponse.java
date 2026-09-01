package com.ticket_online.domain.bookings.dto.response;

import com.ticket_online.domain.bookings.domain.BookingStatus;

public record BookingListResponse(
        Long id, String bookingCode, BookingStatus status, String movieTitle) {}
