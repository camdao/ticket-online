package com.ticket_online.domain.seats.dto.request;

import com.ticket_online.domain.seats.domain.SeatType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request DTO for creating a new seat */
@Getter
@NoArgsConstructor
public class CreateSeatRequest {

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotBlank(message = "Row is required")
    @Pattern(regexp = "^[A-Z]{1,2}$", message = "Row must be 1-2 uppercase letters")
    private String row;

    @NotNull(message = "Seat number is required")
    @Min(value = 1, message = "Seat number must be at least 1")
    private Integer number;

    @NotNull(message = "Seat type is required")
    private SeatType type;

    @NotNull(message = "Base price is required")
    @Min(value = 1000, message = "Base price must be at least 1000")
    private Long basePrice;
}
