package com.ticket_online.domain.booking.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
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

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime expireTime;

    @Version private Long version;

    @Builder(access = AccessLevel.PRIVATE)
    Order(Long userId, Long showId, OrderStatus status, LocalDateTime expireTime) {
        this.userId = userId;
        this.showId = showId;
        this.status = status;
        this.expireTime = expireTime;
    }

    public static Order createOrder(Long userId, Long showId) {
        return Order.builder()
                .userId(userId)
                .showId(showId)
                .status(OrderStatus.PENDING)
                .expireTime(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isPaid() {
        return this.status == OrderStatus.PAID;
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
    }
}
