package com.ticket_online.domain.payments.dto.request;

import com.ticket_online.domain.payments.domain.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Return URL is required")
    private String returnUrl;

    @NotBlank(message = "Cancel URL is required")
    private String cancelUrl;
}
