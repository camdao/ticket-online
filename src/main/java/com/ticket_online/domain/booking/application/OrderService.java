package com.ticket_online.domain.booking.application;

import com.ticket_online.domain.booking.dao.OrderRepository;
import com.ticket_online.domain.booking.dao.OrderSeatRepository;
import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.booking.domain.OrderSeat;
import com.ticket_online.domain.booking.dto.request.CreateOrderRequest;
import com.ticket_online.domain.catalog.dao.SeatRepository;
import com.ticket_online.domain.catalog.dao.ShowRepository;
import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.Show;
import com.ticket_online.domain.payment.application.PaymentService;
import com.ticket_online.domain.payment.dto.PaymentUrlResponse;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.util.HoldSeatResult;
import com.ticket_online.global.util.RedisSeatScripts;
import com.ticket_online.global.util.UserUtil;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OrderService {
    private final RedisSeatScripts redisSeatScripts;
    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;
    private final ShowRepository showRepository;
    private final UserUtil userUtil;
    private final PaymentService paymentService;
    private final SeatRepository seatRepository;

    @Transactional
    public PaymentUrlResponse createOrder(CreateOrderRequest req) {
        User user = userUtil.getCurrentUser();

        if (redisSeatScripts.checkAndExtendSeats(req.showId(), req.seatIds(), user.getId(), 1800)
                != HoldSeatResult.SUCCESS) {
            throw new CustomException(ErrorCode.ORDER_SEAT_HOLD_FAILED);
        }

        Show show =
                showRepository
                        .findById(req.showId())
                        .orElseThrow(() -> new CustomException(ErrorCode.SHOW_NOT_FOUND));

        List<Seat> seats = seatRepository.findAllById(req.seatIds());

        BigDecimal totalAmount =
                seats.stream().map(Seat::getPrice).reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        Order order = Order.createOrder(user, show, totalAmount);
        orderRepository.save(order);

        orderSeatRepository.saveAll(
                req.seatIds().stream()
                        .map(seatId -> OrderSeat.createOrderSeat(order.getId(), seatId))
                        .toList());
        PaymentUrlResponse url = paymentService.createPayment(order);

        return url;
    }

    @Transactional
    public void markOrderAsPaid(Long orderId, Long paymentId) {
        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        order.markPaid();
        order.setPaymentId(paymentId);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        order.markAsCancelled();
        orderRepository.save(order);
    }
}
