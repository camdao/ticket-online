package com.ticket_online.domain.movies.dto;

import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import java.time.LocalDate;
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
    private String genre;
    private String director;
    private String cast;
    private Double rating;
    private String ageRating;
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private MovieStatus status;

    public static MovieResponse from(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .duration(movie.getDuration())
                .description(movie.getDescription())
                .genre(movie.getGenre())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .rating(movie.getRating() != null ? Double.parseDouble(movie.getRating()) : null)
                .ageRating(movie.getAgeRating())
                .releaseDate(movie.getReleaseDate())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .status(movie.getStatus())
                .build();
    }
}
