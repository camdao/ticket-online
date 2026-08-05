package com.ticket_online.domain.bookings.dto.request;

import com.ticket_online.domain.payments.domain.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer phone is required")
    @Pattern(regexp = "^0\\d{9}$", message = "Invalid phone number format")
    private String customerPhone;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
