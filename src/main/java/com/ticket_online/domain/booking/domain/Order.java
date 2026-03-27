package com.ticket_online.domain.booking.domain;

import com.ticket_online.domain.catalog.domain.Show;
import com.ticket_online.domain.model.BaseTimeEntity;
import com.ticket_online.domain.user.domain.User;
import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
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
    @Column(name = "order_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime expireTime;

    private BigDecimal totalAmount;

    private Long paymentId;

    @Version private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Builder(access = AccessLevel.PRIVATE)
    Order(
            User user,
            Show show,
            OrderStatus status,
            LocalDateTime expireTime,
            BigDecimal totalAmount) {
        this.user = user;
        this.show = show;
        this.status = status;
        this.expireTime = expireTime;
    }

    public static Order createOrder(User user, Show show, BigDecimal totalAmount) {
        return Order.builder()
                .user(user)
                .show(show)
                .status(OrderStatus.PENDING)
                .expireTime(LocalDateTime.now().plusMinutes(30))
                .totalAmount(totalAmount)
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

    public void markAsCancelled() {
        this.status = OrderStatus.CANCELLED;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public void updateTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
