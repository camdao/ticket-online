package com.ticket_online.domain.bookings.dto.request;

import com.ticket_online.domain.payments.domain.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    @NotNull(message = "Showtime ID is required")
    private Long showtimeId;

    @NotEmpty(message = "Seat IDs cannot be empty")
    @Size(max = 10, message = "Cannot book more than 10 seats at once")
    private List<Long> seatIds;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
