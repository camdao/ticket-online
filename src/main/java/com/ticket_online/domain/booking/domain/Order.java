package com.ticket_online.domain.booking.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long showId;

    private OrderStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    Order(Long userId, Long showId, OrderStatus status) {
        this.userId = userId;
        this.showId = showId;
        this.status = status;
    }

    public static Order createOrder(Long userId, Long showId) {
        return Order.builder().userId(userId).showId(showId).status(OrderStatus.PENDING).build();
    }
}
