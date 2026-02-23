package com.ticket_online.domain.booking.dto;

public record CreateOrderResponse(Long orderId) {
    public static CreateOrderResponse of(Long orderId) {
        return new CreateOrderResponse(orderId);
    }
}
