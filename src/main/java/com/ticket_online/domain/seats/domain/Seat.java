package com.ticket_online.domain.seats.domain;

import com.ticket_online.domain.cinemas.domain.Screen;
import com.ticket_online.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Entity representing a physical seat in a cinema screen */
@Entity
@Table(
        name = "seats",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"screen_id", "row", "number"})},
        indexes = {
            @Index(name = "idx_seat_screen", columnList = "screen_id"),
            @Index(name = "idx_seat_row_number", columnList = "row, number")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "row", nullable = false, length = 2)
    private String row;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SeatType type;

    @Column(name = "base_price", nullable = false)
    private Long basePrice;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    public Seat(Screen screen, String row, Integer number, SeatType type, Long basePrice) {
        this.screen = screen;
        this.row = row;
        this.number = number;
        this.type = type;
        this.basePrice = basePrice;
        this.isActive = true;
    }

    /** Get the seat label (e.g., "A-5") */
    public String getSeatLabel() {
        return row + "-" + number;
    }

    /** Calculate the price for this seat based on seat type */
    public Long calculatePrice() {
        return basePrice;
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

    /** Update base price */
    public void updateBasePrice(Long newPrice) {
        if (newPrice <= 0) {
            throw new IllegalArgumentException("Base price must be positive");
        }
        this.basePrice = newPrice;
    }
}
