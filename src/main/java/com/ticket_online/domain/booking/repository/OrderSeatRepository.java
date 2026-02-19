package com.ticket_online.domain.booking.repository;

import com.ticket_online.domain.booking.domain.OrderSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSeatRepository extends JpaRepository<OrderSeat, Long> {
}
