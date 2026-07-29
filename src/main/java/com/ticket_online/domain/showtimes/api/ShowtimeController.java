package com.ticket_online.domain.showtimes.api;

import com.ticket_online.domain.cinemas.dto.response.ShowtimeResponse;
import com.ticket_online.domain.showtimes.application.ShowtimeService;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeSeatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
@Tag(name = "Showtimes", description = "Showtime management APIs")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping
    @Operation(
            summary = "Get list of showtimes",
            description =
                    "Returns a list of showtimes filtered by optional parameters. All filters are"
                            + " optional. Returns only active and future showtimes. Date format:"
                            + " YYYY-MM-DD.")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimes(
            @Parameter(description = "Filter by movie ID", example = "1")
                    @RequestParam(required = false)
                    Long movieId,
            @Parameter(description = "Filter by cinema ID", example = "5")
                    @RequestParam(required = false)
                    Long cinemaId,
            @Parameter(description = "Filter by city", example = "TP. Hồ Chí Minh")
                    @RequestParam(required = false)
                    String city,
            @Parameter(description = "Filter by specific date (YYYY-MM-DD)", example = "2024-01-15")
                    @RequestParam(required = false)
                    String date,
            @Parameter(description = "Filter from date (YYYY-MM-DD)", example = "2024-01-15")
                    @RequestParam(required = false)
                    String startDate,
            @Parameter(description = "Filter to date (YYYY-MM-DD)", example = "2024-01-20")
                    @RequestParam(required = false)
                    String endDate) {
        List<ShowtimeResponse> response =
                showtimeService.getShowtimes(movieId, cinemaId, city, date, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/seats")
    @Operation(
            summary = "Get seat layout for a showtime",
            description =
                    "Returns the complete seat layout and availability status for a specific"
                            + " showtime. Includes screen layout information (rows and seats per"
                            + " row) and detailed information for each seat (type, price,"
                            + " status).")
    public ResponseEntity<ShowtimeSeatsResponse> getShowtimeSeats(
            @Parameter(description = "ID of the showtime", required = true, example = "101")
                    @PathVariable
                    Long id) {
        ShowtimeSeatsResponse response = showtimeService.getShowtimeSeats(id);
        return ResponseEntity.ok(response);
    }
}
