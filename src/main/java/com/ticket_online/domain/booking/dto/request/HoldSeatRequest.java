package com.ticket_online.domain.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record HoldSeatRequest(
        @NotNull(message = "showId not null.") Long showId,
        @NotNull(message = "seatIds not null.")
                @Size(max = 4, message = "seatIds must contain at most 4 items")
                List<Long> seatIds) {}
