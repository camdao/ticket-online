package com.ticket_online.domain.catalog.domain;

import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    private String seatCode;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    Seat(Show show, String seatCode, SeatStatus status) {
        this.show = show;
        this.seatCode = seatCode;
        this.status = status;
    }

    public static Seat createSeat(Show show, String seatCode) {
        return Seat.builder().show(show).seatCode(seatCode).status(SeatStatus.AVAILABLE).build();
    }

    public boolean isSold() {
        return this.status == SeatStatus.SOLD;
    }

    public void markSold() {
        if (this.status == SeatStatus.SOLD) {
            throw new RuntimeException("Seat is already sold");
        }
        this.status = SeatStatus.SOLD;
    }
}
