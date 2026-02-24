package com.ticket_online.domain.payment.api;

import com.ticket_online.domain.booking.application.OrderService;
import com.ticket_online.domain.payment.domain.PayStatus;
import com.ticket_online.domain.payment.dto.PaymentRequest;
import com.ticket_online.domain.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final OrderService orderService;

    @PostMapping("/success")
    public ResponseEntity<PaymentResponse> paymentSuccess(@RequestBody PaymentRequest req) {
        orderService.handlePaymentSuccess(req.orderId());
        return ResponseEntity.ok(PaymentResponse.of(req.orderId(), PayStatus.SUCCESS));
    }
}
