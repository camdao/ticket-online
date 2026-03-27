package com.ticket_online.domain.payment.listener;

import org.springframework.context.ApplicationEvent;

public class PaySuccessEvent extends ApplicationEvent {

    private final String orderId;

    public PaySuccessEvent(Object source, String orderId) {
        super(source);
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}
