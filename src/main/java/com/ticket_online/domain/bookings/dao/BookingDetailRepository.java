package com.ticket_online.domain.bookings.dao;

import com.ticket_online.domain.bookings.domain.BookingDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {

    @Query(
            """
        SELECT bd.seat.id
        FROM BookingDetail bd
        WHERE bd.booking.showtime.id = :showtimeId
        AND bd.booking.status = 'CONFIRMED'
    """)
    List<Long> findConfirmedSeatIdsByShowtimeId(@Param("showtimeId") Long showtimeId);

    @Query(
            """
        SELECT bd.seat.id
        FROM BookingDetail bd
        WHERE bd.booking.id = :bookingId
    """)
    List<Long> findSeatIdsByBookingId(@Param("bookingId") Long bookingId);

    @Query(
            """
    SELECT COUNT(bd) > 0
    FROM BookingDetail bd
    JOIN bd.booking b
    WHERE b.showtime.id = :showtimeId
      AND bd.seat.id IN :seatIds
      AND (
          b.status = 'CONFIRMED'
          OR (
              b.status = 'PENDING'
              AND b.expiresAt > CURRENT_TIMESTAMP
          )
      )
""")
    boolean existsActiveBooking(
            @Param("showtimeId") Long showtimeId, @Param("seatIds") List<Long> seatIds);

    List<BookingDetail> findByBookingId(Long bookingId);
}
