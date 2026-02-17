package com.ticket_online.domain.booking.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long seatId;

    @Builder(access = AccessLevel.PRIVATE)
    OrderSeat(Long orderId, Long seatId) {
        this.orderId = orderId;
        this.seatId = seatId;
    }

    public static OrderSeat createOrderSeat(Long orderId, Long seatId) {
        return OrderSeat.builder().orderId(orderId).seatId(seatId).build();
    }
}
