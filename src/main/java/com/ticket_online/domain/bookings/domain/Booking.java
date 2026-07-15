package com.ticket_online.domain.bookings.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.user.domain.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "bookings",
        indexes = {
            @Index(name = "idx_booking_user", columnList = "user_id"),
            @Index(name = "idx_booking_showtime", columnList = "showtime_id"),
            @Index(name = "idx_booking_code", columnList = "booking_code"),
            @Index(name = "idx_booking_status", columnList = "status"),
            @Index(name = "idx_booking_created", columnList = "created_at")
        })
public class Booking extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 50)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Builder(access = AccessLevel.PRIVATE)
    Booking(
            String bookingCode,
            User user,
            Showtime showtime,
            BigDecimal totalAmount,
            BookingStatus status,
            LocalDateTime expiresAt,
            String customerEmail,
            String customerPhone) {
        this.bookingCode = bookingCode;
        this.user = user;
        this.showtime = showtime;
        this.totalAmount = totalAmount;
        this.status = status;
        this.expiresAt = expiresAt;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
    }

    public static Booking createBooking(
            String bookingCode,
            User user,
            Showtime showtime,
            BigDecimal totalAmount,
            String customerEmail,
            String customerPhone) {
        return Booking.builder()
                .bookingCode(bookingCode)
                .user(user)
                .showtime(showtime)
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // 15 minutes to pay
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .build();
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.expiresAt = null; // Remove expiration after confirmation
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public void expire() {
        this.status = BookingStatus.EXPIRED;
    }

    public boolean isPending() {
        return this.status == BookingStatus.PENDING;
    }

    public boolean isConfirmed() {
        return this.status == BookingStatus.CONFIRMED;
    }

    public boolean isCancelled() {
        return this.status == BookingStatus.CANCELLED;
    }

    public boolean isExpired() {
        return this.status == BookingStatus.EXPIRED;
    }

    public boolean canBeCancelled() {
        if (!isConfirmed()) {
            return isPending();
        }
        // Can cancel confirmed booking if more than 2 hours before showtime
        LocalDateTime showtimeStart = showtime.getStartTime();
        LocalDateTime twoHoursBefore = showtimeStart.minusHours(2);
        return LocalDateTime.now().isBefore(twoHoursBefore);
    }

    public boolean hasExpired() {
        return isPending() && expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}