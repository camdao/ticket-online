package com.ticket_online.domain.payment.listener;

/**
 * DEPRECATED - Event-driven approach removed
 *
 * <p>Payment flow is now DIRECT ORCHESTRATION: PaymentController →
 * PaymentService.validateVnpayIpn() → PaymentFacade.handlePaymentSuccess()
 *
 * <p>No event publishing needed anymore. This listener is kept for reference only and should be
 * deleted.
 */
@Deprecated(forRemoval = true)
public class OrderListener {
    // Removed: No longer needed with Direct Orchestration approach
}
