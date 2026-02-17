package com.ticket_online.domain.payment.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
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
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private PayProvider provider;

    private Long amount;

    private PayStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    Payment(Long orderId, PayProvider provider, Long amount, PayStatus status) {
        this.orderId = orderId;
        this.provider = provider;
        this.amount = amount;
        this.status = status;
    }

    public static Payment createPayment(
            Long orderId, PayProvider provider, Long amount, PayStatus status) {
        return Payment.builder()
                .orderId(orderId)
                .provider(provider)
                .amount(amount)
                .status(status)
                .build();
    }
}
