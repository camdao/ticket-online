package com.ticket_online.domain.booking.dto;

import lombok.Getter;

@Getter
public record PaymentRequest(
        Long orderId
) {
}
