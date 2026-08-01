package com.ticket_online.domain.seats.dto.request;

import com.ticket_online.domain.seats.domain.SeatType;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request DTO for updating a seat */
@Getter
@NoArgsConstructor
public class UpdateSeatRequest {

    private SeatType type;

    @Min(value = 0, message = "Surcharge cannot be negative")
    private Long surcharge;

    private Boolean isActive;
}
