package com.ticket_online.domain.movies.dto;

import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Movie information response")
public record MovieResponse(
        @Schema(description = "Movie ID", example = "1") Long id,
        @Schema(description = "Movie title", example = "The Shawshank Redemption") String title,
        @Schema(description = "Movie duration in minutes", example = "142") Integer duration,
        @Schema(
                        description = "Movie description",
                        example = "Two imprisoned men bond over a number of years...")
                String description,
        @Schema(description = "Movie genre", example = "Drama") String genre,
        @Schema(description = "Movie director", example = "Frank Darabont") String director,
        @Schema(description = "Movie cast", example = "Tim Robbins, Morgan Freeman") String cast,
        @Schema(description = "Age rating", example = "R") String ageRating,
        @Schema(description = "Release date", example = "1994-09-23") LocalDate releaseDate,
        @Schema(description = "Movie poster URL", example = "https://example.com/poster.jpg")
                String posterUrl,
        @Schema(description = "Movie trailer URL", example = "https://example.com/trailer.mp4")
                String trailerUrl,
        @Schema(description = "Movie status") MovieStatus status) {

    public static MovieResponse from(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDuration(),
                movie.getDescription(),
                movie.getGenre(),
                movie.getDirector(),
                movie.getCast(),
                movie.getRating(),
                movie.getReleaseDate(),
                movie.getImageUrl(),
                movie.getTrailerUrl(),
                movie.getStatus());
    }
}
