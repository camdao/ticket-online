package com.ticket_online.domain.booking.repository;

import com.ticket_online.domain.booking.domain.OrderSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderSeatRepository extends JpaRepository<OrderSeat, Long> {
    @Query("""
        select os.seatId
        from OrderSeat os
        where os.orderId = :orderId
    """)
    List<Long> findSeatIdsByOrderId(Long orderId);
}
