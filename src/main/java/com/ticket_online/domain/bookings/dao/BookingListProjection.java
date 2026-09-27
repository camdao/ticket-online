package com.ticket_online.domain.bookings.dao;

import com.ticket_online.domain.bookings.domain.BookingStatus;

public interface BookingListProjection {
    Long getId();

    String getBookingCode();

    BookingStatus getStatus();

    String getMovieTitle();
}
