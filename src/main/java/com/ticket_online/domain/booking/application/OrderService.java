package com.ticket_online.domain.booking.application;

import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.booking.domain.OrderSeat;
import com.ticket_online.domain.booking.repository.OrderRepository;
import com.ticket_online.domain.booking.repository.OrderSeatRepository;
import com.ticket_online.domain.catalog.reponsitory.SeatRepository;
import com.ticket_online.domain.payment.dto.PaymentRequest;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.util.HoldSeatResult;
import com.ticket_online.global.util.RedisSeatScripts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OrderService {
    private final RedisSeatScripts redisSeatScripts;
    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;
    @Transactional
    public Long createOrder(Long showId , List<Long> seatIds, Long userId) {
        if (redisSeatScripts.checkAndExtendSeats(
                showId,
                seatIds,
                userId,
                1800
        ) != HoldSeatResult.SUCCESS) {
            throw new CustomException(ErrorCode.ORDER_SEAT_HOLD_FAILED);
        }

        Order order = Order.createOrder(userId,showId);
        orderRepository.save(order);

        orderSeatRepository.saveAll(
                seatIds.stream()
                        .map(seatId -> OrderSeat.createOrderSeat(order.getId(), seatId))
                        .toList()
        );
        return order.getId();
    }

    @Transactional
    public void handlePaymentSuccess(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        if (order.isPaid()) {
            return;
        }

        order.markPaid();
        orderRepository.save(order);

    }
}
