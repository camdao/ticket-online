package com.ticket_online.domain.payment.listener;

import com.ticket_online.domain.booking.application.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderListener {
    private final OrderService orderService;
    @EventListener
    public void handleCustomEvent(PaySuccessEvent event) {
        System.out.println("Received custom event: " + event.getOrderId());
        orderService.handlePaymentSuccess(Long.parseLong(event.getOrderId()));
    }
}
