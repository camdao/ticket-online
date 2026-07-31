package com.ticket_online.domain.movies.dto;

import com.ticket_online.domain.movies.domain.Movie;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "List of movies response")
public record MovieListResponse(
        @Schema(description = "List of movies") List<MovieResponse> content,
        @Schema(description = "Total number of movies", example = "100") long totalElements) {

    public static MovieListResponse of(List<Movie> movies) {
        List<MovieResponse> content =
                movies.stream().map(MovieResponse::from).collect(Collectors.toList());
        return new MovieListResponse(content, movies.size());
    }
}
