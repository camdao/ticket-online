package com.ticket_online.domain.seats.domain;

import com.ticket_online.domain.cinemas.domain.Room;
import com.ticket_online.domain.model.BaseTimeEntity;
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
        name = "seats",
        indexes = {
            @Index(name = "idx_seat_room", columnList = "room_id"),
            @Index(name = "idx_seat_position", columnList = "room_id, row_code, seat_number")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_seat_position",
                    columnNames = {"room_id", "row_code", "seat_number"})
        })
public class Seat extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "row_code", nullable = false, length = 10)
    private String rowCode;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 20)
    private SeatType seatType;

    @Column(name = "surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal surcharge;

    @Builder(access = AccessLevel.PRIVATE)
    Seat(Room room, String rowCode, Integer seatNumber, SeatType seatType, BigDecimal surcharge) {
        this.room = room;
        this.rowCode = rowCode;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.surcharge = surcharge;
    }

    public static Seat createSeat(
            Room room, String rowCode, Integer seatNumber, SeatType seatType, BigDecimal surcharge) {
        return Seat.builder()
                .room(room)
                .rowCode(rowCode)
                .seatNumber(seatNumber)
                .seatType(seatType)
                .surcharge(surcharge != null ? surcharge : BigDecimal.ZERO)
                .build();
    }

    public void updateSeat(String rowCode, Integer seatNumber, SeatType seatType, BigDecimal surcharge) {
        this.rowCode = rowCode;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.surcharge = surcharge;
    }

    public boolean isVipSeat() {
        return this.seatType == SeatType.VIP;
    }

    public boolean isCoupleSeat() {
        return this.seatType == SeatType.COUPLE;
    }

    public String getPosition() {
        return rowCode + seatNumber;
    }
}