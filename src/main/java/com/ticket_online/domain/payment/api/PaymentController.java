package com.ticket_online.domain.payment.api;

import com.ticket_online.domain.payment.application.PaymentFacade;
import com.ticket_online.domain.payment.application.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentFacade paymentFacade;

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayIpn(HttpServletRequest request) {
        Long orderId = paymentService.validateVnpayIpn(request);
        paymentFacade.handlePaymentSuccess(orderId);
        return ResponseEntity.ok("OK");
    }
}
