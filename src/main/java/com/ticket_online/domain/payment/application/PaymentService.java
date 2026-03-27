package com.ticket_online.domain.payment.application;

import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.payment.dao.PaymentRepository;
import com.ticket_online.domain.payment.domain.PayProvider;
import com.ticket_online.domain.payment.domain.PayStatus;
import com.ticket_online.domain.payment.domain.Payment;
import com.ticket_online.domain.payment.dto.PaymentUrlResponse;
import com.ticket_online.domain.payment.strategy.PaymentStrategy;
import com.ticket_online.domain.payment.strategy.PaymentStrategyFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;

    @Transactional
    public PaymentUrlResponse createPayment(Order order) {
        order.updateTotalAmount(BigDecimal.valueOf(10000000));
        Payment payment =
                Payment.createPayment(
                        order,
                        PayProvider.VNPAY,
                        order.getTotalAmount().longValue(),
                        PayStatus.PENDING);
        PaymentStrategy paymentMethod =
                paymentStrategyFactory.getPaymentStrategy(PayProvider.VNPAY.name());
        PaymentUrlResponse url = paymentMethod.createPayment(order);
        paymentRepository.save(payment);
        return url;
    }

    public Long validateVnpayIpn(HttpServletRequest request) {
        PaymentStrategy paymentMethod =
                paymentStrategyFactory.getPaymentStrategy(PayProvider.VNPAY.name());
        Map<String, String> params = extractParams(request);

        paymentMethod.handleCallback(params);

        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        if (!"00".equals(responseCode)) {
            throw new RuntimeException("Payment failed with response code: " + responseCode);
        }

        return Long.parseLong(orderId);
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();

        request.getParameterMap()
                .forEach(
                        (key, values) -> {
                            if (values != null && values.length > 0) {
                                params.put(key, values[0]);
                            }
                        });

        return params;
    }

    @Transactional
    public void markPaymentSuccess(Long paymentId) {
        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.confirmPayment();
        paymentRepository.save(payment);
    }

    @Transactional
    public void markPaymentFailed(Long paymentId, String reason) {
        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.markAsFailed(reason);
        paymentRepository.save(payment);
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }
}
