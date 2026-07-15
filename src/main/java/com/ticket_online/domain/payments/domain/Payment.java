package com.ticket_online.domain.payments.domain;

import com.ticket_online.domain.bookings.domain.Booking;
import com.ticket_online.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "payments",
        indexes = {
            @Index(name = "idx_payment_booking", columnList = "booking_id"),
            @Index(name = "idx_payment_transaction", columnList = "transaction_id"),
            @Index(name = "idx_payment_status", columnList = "status")
        })
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_url", length = 500)
    private String paymentUrl;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder(access = AccessLevel.PRIVATE)
    Payment(
            Booking booking,
            String transactionId,
            PaymentMethod paymentMethod,
            BigDecimal amount,
            PaymentStatus status,
            String paymentUrl,
            LocalDateTime expiresAt) {
        this.booking = booking;
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
        this.paymentUrl = paymentUrl;
        this.expiresAt = expiresAt;
    }

    public static Payment createPayment(
            Booking booking,
            String transactionId,
            PaymentMethod paymentMethod,
            BigDecimal amount,
            String paymentUrl) {
        return Payment.builder()
                .booking(booking)
                .transactionId(transactionId)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .paymentUrl(paymentUrl)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
    }

    public void markAsSuccess(String gatewayResponse) {
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = LocalDateTime.now();
        this.gatewayResponse = gatewayResponse;
        this.expiresAt = null;
    }

    public void markAsFailed(String gatewayResponse) {
        this.status = PaymentStatus.FAILED;
        this.gatewayResponse = gatewayResponse;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }

    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }
}