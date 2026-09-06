package com.ticket_online.domain.bookings.dao;

import com.ticket_online.domain.bookings.domain.Booking;
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
        SELECT
            b.id AS id,
            b.bookingCode AS bookingCode,
            b.status AS status,
            m.title AS movieTitle
        FROM Booking b
        JOIN b.showtime s
        JOIN s.movie m
        WHERE b.user.id = :userId
        ORDER BY b.createdAt DESC
    """)
    Page<BookingListProjection> findUserBookings(@Param("userId") Long userId, Pageable pageable);

    @Query(
            """
        SELECT b
        FROM Booking b
        WHERE b.status = 'PENDING'
        AND b.expiresAt < :now
        ORDER BY b.expiresAt
    """)
    List<Booking> findExpiredPendingBookings(@Param("now") LocalDateTime now);

    @Query(
            """
        SELECT
            b.id AS bookingId,
            b.bookingCode AS bookingCode,
            m.title AS movieTitle,
            m.imageUrl AS movieImageUrl,
            b.totalAmount AS totalAmount,
            b.status AS status,
            b.createdAt AS createdAt,
            b.confirmedAt AS confirmedAt,
            s.startTime AS startTime,
            s.endTime AS endTime,
            seat.id AS seatId,
            seat.row AS seatRow,
            seat.number AS seatNumber,
            seat.type AS seatType,
            bd.price AS price
        FROM BookingDetail bd
        JOIN bd.booking b
        JOIN b.showtime s
        JOIN s.movie m
        JOIN bd.seat seat
        WHERE b.id = :bookingId
        AND b.user.id = :userId
    """)
    List<BookingDetailProjection> findBookingDetail(
            @Param("bookingId") Long bookingId, @Param("userId") Long userId);
}
