package com.ticket_online.domain.payments.api;

import com.ticket_online.domain.payments.application.PaymentService;
import com.ticket_online.domain.payments.dto.request.PaymentRequest;
import com.ticket_online.domain.payments.dto.response.PaymentResponse;
import com.ticket_online.domain.payments.dto.response.PaymentVerificationResponse;
import com.ticket_online.global.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request, HttpServletRequest httpRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        PaymentResponse response = paymentService.initiatePayment(request, userId, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/callback")
    public RedirectView handlePaymentCallback(@RequestParam Map<String, String> params) {
        try {
            paymentService.handleVnpayCallback(params);

            // Redirect to success page
            String returnUrl = params.getOrDefault("returnUrl", "/booking-success");
            return new RedirectView(returnUrl);
        } catch (Exception e) {
            log.error("Error handling payment callback", e);
            // Redirect to error page
            return new RedirectView("/booking-error");
        }
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentVerificationResponse> verifyPayment(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        PaymentVerificationResponse response = paymentService.verifyPayment(id, userId);
        return ResponseEntity.ok(response);
    }
}