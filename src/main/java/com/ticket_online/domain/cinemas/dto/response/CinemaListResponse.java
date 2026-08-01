package com.ticket_online.domain.cinemas.dto.response;

import com.ticket_online.domain.cinemas.domain.Cinema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Schema(description = "List of cinemas")
public record CinemaListResponse(
        @Schema(description = "List of cinemas") List<CinemaResponse> cinemas) {

    public static CinemaListResponse of(List<Cinema> cinemas, Map<Long, Integer> roomCountMap) {
        List<CinemaResponse> cinemaResponses =
                cinemas.stream()
                        .map(
                                cinema -> {
                                    Integer totalRooms =
                                            roomCountMap.getOrDefault(cinema.getId(), 0);
                                    return CinemaResponse.from(cinema, totalRooms);
                                })
                        .collect(Collectors.toList());
        return new CinemaListResponse(cinemaResponses);
    }
}
