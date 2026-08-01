package com.ticket_online.domain.seats.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import com.ticket_online.domain.rooms.domain.Room;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Entity representing a physical seat in a cinema room */
@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"room_id", "row_label", "seat_number"})
        },
        indexes = {
            @Index(name = "idx_seat_room", columnList = "room_id"),
            @Index(name = "idx_seat_row_number", columnList = "row, number")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "row_label", nullable = false, length = 5)
    private String row;

    @Column(name = "seat_number", nullable = false)
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 20)
    private SeatType type;

    @Column(name = "surcharge", nullable = false)
    private Long surcharge;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    public Seat(Room room, String row, Integer number, SeatType type, Long surcharge) {
        this.room = room;
        this.row = row;
        this.number = number;
        this.type = type;
        this.surcharge = surcharge;
        this.isActive = true;
    }

    /** Get the seat label (e.g., "A-5") */
    public String getSeatLabel() {
        return row + "-" + number;
    }

    /** Calculate the total price for this seat including surcharge */
    public Long calculatePrice(Long showtimeBasePrice) {
        return showtimeBasePrice + surcharge;
    }

    /** Deactivate the seat */
    public void deactivate() {
        this.isActive = false;
    }

    /** Activate the seat */
    public void activate() {
        this.isActive = true;
    }

    /** Update seat type */
    public void updateType(SeatType newType) {
        this.type = newType;
    }

    /** Update surcharge for special seat types */
    public void updateSurcharge(Long newSurcharge) {
        if (newSurcharge < 0) {
            throw new IllegalArgumentException("Surcharge cannot be negative");
        }
        this.surcharge = newSurcharge;
    }
}
