package com.ticket_online.domain.movies.dto;

import com.ticket_online.domain.movies.domain.Movie;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

    private Long id;
    private String title;
    private Integer duration;
    private String description;
    private String imageUrl;
    private String trailerUrl;
    private LocalDate releaseDate;
    private String genre;
    private String director;
    private String cast;
    private String rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MovieResponse from(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .duration(movie.getDuration())
                .description(movie.getDescription())
                .imageUrl(movie.getImageUrl())
                .trailerUrl(movie.getTrailerUrl())
                .releaseDate(movie.getReleaseDate())
                .genre(movie.getGenre())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .rating(movie.getRating())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
