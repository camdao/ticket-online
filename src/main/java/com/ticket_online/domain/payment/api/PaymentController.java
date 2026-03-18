package com.ticket_online.domain.payment.api;

import com.ticket_online.domain.booking.application.OrderService;
import com.ticket_online.domain.payment.application.PaymentService;
import com.ticket_online.domain.payment.domain.PayStatus;
import com.ticket_online.domain.payment.dto.PaymentRequest;
import com.ticket_online.domain.payment.dto.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping("/success")
    public ResponseEntity<PaymentResponse> paymentSuccess(@RequestBody PaymentRequest req) {
        orderService.handlePaymentSuccess(req.orderId());
        return ResponseEntity.ok(PaymentResponse.of(req.orderId(), PayStatus.SUCCESS));
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> ipn(HttpServletRequest request) {
        paymentService.handleVnpayIpn(request);
        return ResponseEntity.ok("OK");
    }
}
