package com.ticket_online.domain.payment.strategy;

import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.payment.dto.PaymentUrlResponse;
import java.util.Map;

public interface PaymentStrategy {
    PaymentUrlResponse createPayment(Order order);

    void handleCallback(Map<String, String> params);
}
