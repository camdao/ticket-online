package com.ticket_online.domain.bookings.dao;

import com.ticket_online.domain.bookings.domain.Booking;
import com.ticket_online.domain.bookings.domain.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.showtime.id = :showtimeId")
    List<Booking> findByShowtimeId(@Param("showtimeId") Long showtimeId);

    @Query("SELECT b FROM Booking b WHERE b.showtime.id = :showtimeId AND b.status = 'CONFIRMED'")
    List<Booking> findConfirmedByShowtimeId(@Param("showtimeId") Long showtimeId);

    @Query(
            "SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.expiresAt < :now ORDER BY"
                    + " b.expiresAt")
    List<Booking> findExpiredPendingBookings(@Param("now") LocalDateTime now);

    @Query(
            "SELECT COUNT(b) FROM Booking b WHERE b.showtime.id = :showtimeId AND b.status ="
                    + " 'CONFIRMED'")
    long countConfirmedBookingsByShowtimeId(@Param("showtimeId") Long showtimeId);

    boolean existsByBookingCode(String bookingCode);
}
