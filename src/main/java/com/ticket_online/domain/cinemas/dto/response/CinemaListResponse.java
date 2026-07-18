package com.ticket_online.domain.cinemas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated list of cinemas")
public record CinemaListResponse(
        @Schema(description = "List of cinemas on the current page") List<CinemaResponse> content,
        @Schema(description = "Current page number (0-indexed)", example = "0") int page,
        @Schema(description = "Number of items per page", example = "10") int size,
        @Schema(description = "Total number of cinemas across all pages", example = "45")
                long totalElements,
        @Schema(description = "Total number of pages", example = "5") int totalPages) {}
