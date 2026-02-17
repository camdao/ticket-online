package com.ticket_online.domain.catalog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String showId;

    private String seatCode;

    private SeatStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    Seat(String showId, String seatCode, SeatStatus status) {
        this.showId = showId;
        this.seatCode = seatCode;
        this.status = status;
    }

    public static Seat createSeat(String showId, String seatCode) {
        return Seat.builder()
                .showId(showId)
                .seatCode(seatCode)
                .status(SeatStatus.AVAILABLE).build();
    }
}
