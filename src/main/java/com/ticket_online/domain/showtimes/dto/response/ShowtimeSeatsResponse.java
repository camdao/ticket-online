package com.ticket_online.domain.showtimes.dto.response;

import com.ticket_online.domain.seats.dto.response.SeatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "Response containing showtime seats and availability")
@Getter
@Builder
public class ShowtimeSeatsResponse {

    @Schema(description = "Showtime identifier", example = "101")
    private Long showtimeId;

    @Schema(description = "List of seats with their details and availability")
    private List<SeatResponse> seats;

    public static ShowtimeSeatsResponse of(Long showtimeId, List<SeatResponse> seats) {
        return ShowtimeSeatsResponse.builder().showtimeId(showtimeId).seats(seats).build();
    }
}
