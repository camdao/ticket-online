package com.ticket_online.domain.payment.dto;

import com.ticket_online.domain.payment.domain.PayStatus;

public record PaymentResponse(Long orderId, PayStatus status) {
    public static PaymentResponse of(Long orderId, PayStatus status) {
        return new PaymentResponse(orderId, status);
    }
}
