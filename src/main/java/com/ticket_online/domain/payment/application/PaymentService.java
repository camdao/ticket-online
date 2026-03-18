package com.ticket_online.domain.payment.application;

import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.payment.dao.PaymentRepository;
import com.ticket_online.domain.payment.domain.PayProvider;
import com.ticket_online.domain.payment.domain.PayStatus;
import com.ticket_online.domain.payment.domain.Payment;
import com.ticket_online.domain.payment.dto.PaymentUrlResponse;
import com.ticket_online.domain.payment.strategy.PaymentStrategy;
import com.ticket_online.domain.payment.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;

    public PaymentUrlResponse createPayment(Order orderId) {
        Payment payment = Payment.createPayment(orderId, PayProvider.VNPAY, 0L, PayStatus.PENDING);
        PaymentStrategy paymentMethod =
                paymentStrategyFactory.getPaymentStrategy(PayProvider.VNPAY.name());
        PaymentUrlResponse url = paymentMethod.createPayment(orderId);
        paymentRepository.save(payment);
        return url;
    }
}
