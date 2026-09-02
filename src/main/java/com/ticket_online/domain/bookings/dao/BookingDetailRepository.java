package com.ticket_online.domain.bookings.dao;

import com.ticket_online.domain.bookings.domain.BookingDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {

    @Query(
            "SELECT bd.seat.id FROM BookingDetail bd WHERE bd.booking.showtime.id = :showtimeId"
                    + " AND bd.booking.status = 'CONFIRMED'")
    List<Long> findConfirmedSeatIdsByShowtimeId(@Param("showtimeId") Long showtimeId);

    List<BookingDetail> findByBookingId(Long bookingId);

    @Query(
            """
    SELECT bd.seat.id
    FROM BookingDetail bd
    WHERE bd.booking.id = :bookingId
""")
    List<Long> findSeatIdsByBookingId(@Param("bookingId") Long bookingId);
}
