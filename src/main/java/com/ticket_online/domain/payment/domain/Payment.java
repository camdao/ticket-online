package com.ticket_online.domain.payment.domain;

import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private PayProvider provider;

    private Long amount;

    private PayStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    Payment(Order order, PayProvider provider, Long amount, PayStatus status) {
        this.order = order;
        this.provider = provider;
        this.amount = amount;
        this.status = status;
    }

    public static Payment createPayment(
            Order order, PayProvider provider, Long amount, PayStatus status) {
        return Payment.builder()
                .order(order)
                .provider(provider)
                .amount(amount)
                .status(status)
                .build();
    }

    public void confirmPayment() {
        this.status = PayStatus.SUCCESS;
    }
}
