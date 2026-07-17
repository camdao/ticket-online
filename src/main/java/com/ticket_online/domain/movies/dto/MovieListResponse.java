package com.ticket_online.domain.movies.dto;

import com.ticket_online.domain.movies.domain.Movie;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

@Schema(description = "Paginated list of movies response")
public record MovieListResponse(
        @Schema(description = "List of movies in the current page") List<MovieResponse> content,
        @Schema(description = "Current page number (0-indexed)", example = "0") int page,
        @Schema(description = "Number of items per page", example = "10") int size,
        @Schema(description = "Total number of movies", example = "100") long totalElements,
        @Schema(description = "Total number of pages", example = "10") int totalPages) {

    public static MovieListResponse of(Page<Movie> moviePage) {
        List<MovieResponse> content =
                moviePage.getContent().stream()
                        .map(MovieResponse::from)
                        .collect(Collectors.toList());
        return new MovieListResponse(
                content,
                moviePage.getNumber(),
                moviePage.getSize(),
                moviePage.getTotalElements(),
                moviePage.getTotalPages());
    }
}
