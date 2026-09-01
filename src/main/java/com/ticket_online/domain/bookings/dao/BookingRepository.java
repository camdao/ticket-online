package com.ticket_online.domain.bookings.dao;

import com.ticket_online.domain.bookings.domain.Booking;
import com.ticket_online.domain.bookings.dto.response.BookingDetailRow;
import com.ticket_online.domain.bookings.dto.response.BookingListResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(
            """
    SELECT new com.ticket_online.domain.bookings.dto.response.BookingListResponse(
        b.id,
        b.bookingCode,
        b.status,
        m.title
    )
    FROM Booking b
    JOIN b.showtime s
    JOIN s.movie m
    WHERE b.user.id = :userId
    ORDER BY b.createdAt DESC
""")
    Page<BookingListResponse> findUserBookings(@Param("userId") Long userId, Pageable pageable);

    @Query(
            "SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.expiresAt < :now ORDER BY"
                    + " b.expiresAt")
    List<Booking> findExpiredPendingBookings(@Param("now") LocalDateTime now);

    @Query(
            """
    SELECT new com.ticket_online.domain.bookings.dto.response.BookingDetailRow(
        b.id,
        b.bookingCode,
        m.title,
        m.imageUrl,
        b.totalAmount,
        b.status,
        b.createdAt,
        b.confirmedAt,
        s.startTime,
        s.endTime,
        seat.id,
        seat.rowLabel,
        seat.seatNumber,
        seat.seatType,
        bd.price
    )
    FROM Booking b
    JOIN b.showtime s
    JOIN s.movie m
    JOIN b.bookingDetails bd
    JOIN bd.seat seat
    WHERE b.id = :bookingId
      AND b.user.id = :userId
""")
    List<BookingDetailRow> findBookingDetail(
            @Param("bookingId") Long bookingId, @Param("userId") Long userId);
}
