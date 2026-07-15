package com.ticket_online.domain.bookings.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    @NotBlank(message = "Hold token is required")
    private String holdToken;

    @NotNull(message = "Showtime ID is required")
    private Long showtimeId;

    @NotEmpty(message = "Seat IDs cannot be empty")
    private List<Long> seatIds;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer phone is required")
    @Pattern(regexp = "^0\\d{9}$", message = "Invalid phone number format")
    private String customerPhone;
}