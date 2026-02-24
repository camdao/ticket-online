package com.ticket_online.domain.booking.api;

import com.ticket_online.domain.booking.application.OrderService;
import com.ticket_online.domain.booking.dto.CreateOrderRequest;
import com.ticket_online.domain.booking.dto.CreateOrderResponse;
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
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest req) {
        Long id = orderService.createOrder(req.showId(), req.seatIds(), req.userId());
        return ResponseEntity.ok(new CreateOrderResponse(id));
    }
}
