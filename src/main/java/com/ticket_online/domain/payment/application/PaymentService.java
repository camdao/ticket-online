package com.ticket_online.domain.payment.application;

import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.payment.dao.PaymentRepository;
import com.ticket_online.domain.payment.domain.PayProvider;
import com.ticket_online.domain.payment.domain.PayStatus;
import com.ticket_online.domain.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public void createPayment(Order orderId) {
        Payment payment = Payment.createPayment(orderId, PayProvider.FAKE, 0L, PayStatus.SUCCESS);
        paymentRepository.save(payment);
    }
}
