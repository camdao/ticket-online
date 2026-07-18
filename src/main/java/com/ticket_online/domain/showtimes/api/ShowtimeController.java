package com.ticket_online.domain.showtimes.api;

import com.ticket_online.domain.showtimes.application.ShowtimeService;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeSeatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
@Tag(name = "Showtimes", description = "Showtime management APIs")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping("/{id}/seats")
    @Operation(
            summary = "Get seat layout for a showtime",
            description =
                    "Returns the complete seat layout and availability status for a specific"
                            + " showtime. Includes screen layout information (rows and seats per"
                            + " row) and detailed information for each seat (type, price,"
                            + " status).")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved seat layout",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                ShowtimeSeatsResponse.class))),
                @ApiResponse(responseCode = "404", description = "Showtime not found")
            })
    public ResponseEntity<ShowtimeSeatsResponse> getShowtimeSeats(
            @Parameter(description = "ID of the showtime", required = true, example = "101")
                    @PathVariable
                    Long id) {
        ShowtimeSeatsResponse response = showtimeService.getShowtimeSeats(id);
        return ResponseEntity.ok(response);
    }
}
