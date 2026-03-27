package com.ticket_online.domain.booking.api;

import com.ticket_online.domain.booking.application.OrderService;
import com.ticket_online.domain.booking.dto.request.CreateOrderRequest;
import com.ticket_online.domain.payment.dto.PaymentUrlResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<PaymentUrlResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest req) {
        return ResponseEntity.ok(orderService.createOrder(req.showId(), req.seatIds()));
    }
}
