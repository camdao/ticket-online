package com.ticket_online.domain.payment.application;

import com.ticket_online.domain.booking.application.OrderService;
import com.ticket_online.domain.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public void handlePaymentSuccess(Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        paymentService.markPaymentSuccess(payment.getId());
        orderService.markOrderAsPaid(orderId, payment.getId());
    }

    public void handlePaymentFailed(Long orderId, String reason) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        paymentService.markPaymentFailed(payment.getId(), reason);
        orderService.cancelOrder(orderId);
    }
}
