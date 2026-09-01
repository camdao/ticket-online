package com.ticket_online.domain.bookings.dto.response;

import com.ticket_online.domain.payments.domain.Payment;

public record BookingResponse(String paymentUrl) {

    public static BookingResponse from(Payment payment) {
        return new BookingResponse(payment.getPaymentUrl());
    }
}
