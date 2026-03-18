package com.ticket_online.domain.payment.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {
    private final VnPayStrategy vnPayStrategy;

    public PaymentStrategy getPaymentStrategy(String provider) {
        return switch (provider) {
            case "VNPAY" -> vnPayStrategy;
            default ->
                    throw new IllegalArgumentException("Unsupported payment provider: " + provider);
        };
    }
}
