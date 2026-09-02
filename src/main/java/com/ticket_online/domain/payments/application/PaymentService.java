package com.ticket_online.domain.payments.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket_online.domain.bookings.dao.BookingDetailRepository;
import com.ticket_online.domain.bookings.dao.BookingRepository;
import com.ticket_online.domain.bookings.domain.Booking;
import com.ticket_online.domain.bookings.domain.BookingDetail;
import com.ticket_online.domain.payments.dao.PaymentRepository;
import com.ticket_online.domain.payments.domain.Payment;
import com.ticket_online.domain.payments.domain.PaymentMethod;
import com.ticket_online.domain.payments.dto.response.PaymentVerificationResponse;
import com.ticket_online.global.config.vnpay.VnpayProperties;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.util.RedisSeatScripts;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final VnpayService vnpayService;
    private final RedisSeatScripts redisSeatScripts;
    private final ObjectMapper objectMapper;
    private final VnpayProperties vnpayProperties;

    @Transactional
    public Payment createPayment(Booking booking, PaymentMethod paymentMethod, String ipAddress) {

        String transactionId = generateTransactionId();

        String orderInfo = "Thanh toan ve phim - Booking: " + booking.getBookingCode();

        String paymentUrl =
                vnpayService.createPaymentUrl(
                        transactionId,
                        booking.getTotalAmount().longValue(),
                        orderInfo,
                        vnpayProperties.returnUrl(),
                        ipAddress);

        Payment payment =
                Payment.createPayment(
                        booking,
                        transactionId,
                        paymentMethod,
                        booking.getTotalAmount(),
                        paymentUrl);

        return paymentRepository.save(payment);
    }

    private String generateTransactionId() {
        String prefix = "PAY";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8);
        return prefix + timestamp.substring(timestamp.length() - 10) + random;
    }

    @Transactional
    public void handleVnpayCallback(Map<String, String> params) {
        // Validate signature
        if (!vnpayService.validateCallback(new HashMap<>(params))) {
            log.error("Invalid VNPay callback signature");
            throw new CustomException(ErrorCode.INVALID_PAYMENT_CALLBACK);
        }

        String transactionId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        // Find payment
        Payment payment =
                paymentRepository
                        .findByTransactionId(transactionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // Prevent duplicate processing (idempotency)
        if (!payment.isPending()) {
            log.info(
                    "Payment {} already processed with status: {}",
                    transactionId,
                    payment.getStatus());
            return;
        }

        try {
            String gatewayResponse = objectMapper.writeValueAsString(params);

            if ("00".equals(responseCode)) {
                // Payment successful
                payment.markAsSuccess(gatewayResponse);
                paymentRepository.save(payment);

                // Confirm booking
                Booking booking = payment.getBooking();
                booking.confirm();
                bookingRepository.save(booking);

                // Release seats from Redis (they're now permanently booked)
                List<BookingDetail> details =
                        bookingDetailRepository.findByBookingId(booking.getId());
                List<Long> seatIds = details.stream().map(bd -> bd.getSeat().getId()).toList();
                redisSeatScripts.releaseSeats(booking.getShowtime().getId(), seatIds);

                log.info("Payment {} completed successfully", transactionId);
            } else {
                // Payment failed
                payment.markAsFailed(gatewayResponse);
                paymentRepository.save(payment);

                log.info("Payment {} failed with code: {}", transactionId, responseCode);
            }
        } catch (Exception e) {
            log.error("Error processing payment callback", e);
            payment.markAsFailed("Error: " + e.getMessage());
            paymentRepository.save(payment);
        }
    }

    public PaymentVerificationResponse verifyPayment(Long paymentId, Long userId) {
        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // Verify ownership
        if (!payment.getBooking().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        return PaymentVerificationResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBooking().getId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
