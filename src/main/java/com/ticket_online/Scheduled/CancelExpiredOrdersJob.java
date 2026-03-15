package com.ticket_online.Scheduled;

import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.booking.repository.OrderRepository;
import com.ticket_online.domain.booking.repository.OrderSeatRepository;
import com.ticket_online.global.util.RedisSeatScripts;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CancelExpiredOrdersJob {
    private final OrderRepository orderRepository;
    private final RedisSeatScripts redisSeatScripts;
    private final OrderSeatRepository orderSeatRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cancelExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();

        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(now);

        if (expiredOrders.isEmpty()) {
            return;
        }

        for (Order order : expiredOrders) {

            order.cancel();

            List<Long> seatIds = orderSeatRepository.findSeatIdsByOrderId(order.getId());

            redisSeatScripts.releaseSeats(order.getShow().getId(), seatIds);

            log.info("Cancelled expired order {}", order.getId());
        }
    }
}
