package com.ticket_online.domain.payments.dto.response;

import com.ticket_online.domain.payments.domain.PaymentMethod;
import com.ticket_online.domain.payments.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long bookingId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentUrl;
    private String transactionId;
    private LocalDateTime expiresAt;
}
