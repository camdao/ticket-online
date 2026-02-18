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
    private Long id;

    private Long showId;

    private String seatCode;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    Seat(Long showId, String seatCode, SeatStatus status) {
        this.showId = showId;
        this.seatCode = seatCode;
        this.status = status;
    }

    public static Seat createSeat(Long showId, String seatCode) {
        return Seat.builder()
                .showId(showId)
                .seatCode(seatCode)
                .status(SeatStatus.AVAILABLE)
                .build();
    }

    public boolean isSold() {
        return this.status == SeatStatus.SOLD;
    }
}
