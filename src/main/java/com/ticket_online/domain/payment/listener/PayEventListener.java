package com.ticket_online.domain.payment.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PayEventListener {

    @EventListener
    public void handleCustomEvent(PaySuccessEvent event) {
        System.out.println("Received custom event: " + event.getOrderId());
    }
}
