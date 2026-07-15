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

    @Min(value = 1000, message = "Base price must be at least 1000")
    private Long basePrice;

    private Boolean isActive;
}
