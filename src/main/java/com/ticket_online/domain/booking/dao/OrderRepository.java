package com.ticket_online.domain.booking.dao;

import com.ticket_online.domain.booking.domain.Order;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
        select o from Order o
        where o.status = 'PENDING'
          and o.expireTime < :now
    """)
    List<Order> findExpiredPendingOrders(@Param("now") LocalDateTime now);
}
