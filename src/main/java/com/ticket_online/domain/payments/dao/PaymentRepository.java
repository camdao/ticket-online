package com.ticket_online.domain.payments.dao;

import com.ticket_online.domain.payments.domain.Payment;
import com.ticket_online.domain.payments.domain.PaymentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByBookingId(Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId AND p.status = :status")
    Optional<Payment> findByBookingIdAndStatus(
            @Param("bookingId") Long bookingId, @Param("status") PaymentStatus status);

    boolean existsByTransactionId(String transactionId);
}