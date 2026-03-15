package com.ticket_online.domain.payment.dao;

import com.ticket_online.domain.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {}
