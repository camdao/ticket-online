package com.ticket_online.domain.booking.repository;

import com.ticket_online.domain.booking.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
