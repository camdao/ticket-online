package com.ticket_online.domain.bookings.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import com.ticket_online.domain.seats.domain.Seat;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "booking_details",
        indexes = {
            @Index(name = "idx_booking_detail_booking", columnList = "booking_id"),
            @Index(name = "idx_booking_detail_seat", columnList = "seat_id")
        })
public class BookingDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Builder(access = AccessLevel.PRIVATE)
    BookingDetail(Booking booking, Seat seat, BigDecimal price) {
        this.booking = booking;
        this.seat = seat;
        this.price = price;
    }

    public static BookingDetail createBookingDetail(Booking booking, Seat seat, BigDecimal price) {
        return BookingDetail.builder().booking(booking).seat(seat).price(price).build();
    }
}
