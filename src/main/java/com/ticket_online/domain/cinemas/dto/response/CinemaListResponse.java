package com.ticket_online.domain.cinemas.dto.response;

import com.ticket_online.domain.cinemas.domain.Cinema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

@Schema(description = "Paginated list of cinemas")
public record CinemaListResponse(
        @Schema(description = "List of cinemas on the current page") List<CinemaResponse> content,
        @Schema(description = "Current page number (0-indexed)", example = "0") int page,
        @Schema(description = "Number of items per page", example = "10") int size,
        @Schema(description = "Total number of cinemas across all pages", example = "45")
                long totalElements,
        @Schema(description = "Total number of pages", example = "5") int totalPages) {

    public static CinemaListResponse of(Page<Cinema> cinemaPage, Map<Long, Integer> roomCountMap) {
        List<CinemaResponse> cinemaResponses =
                cinemaPage.getContent().stream()
                        .map(
                                cinema -> {
                                    Integer totalRooms =
                                            roomCountMap.getOrDefault(cinema.getId(), 0);
                                    return CinemaResponse.from(cinema, totalRooms);
                                })
                        .collect(Collectors.toList());
        return new CinemaListResponse(
                cinemaResponses,
                cinemaPage.getNumber(),
                cinemaPage.getSize(),
                cinemaPage.getTotalElements(),
                cinemaPage.getTotalPages());
    }
}
