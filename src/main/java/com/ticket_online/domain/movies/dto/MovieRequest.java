package com.ticket_online.domain.movies.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer duration;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 1000, message = "Poster URL must not exceed 1000 characters")
    private String posterUrl;

    @Size(max = 1000, message = "Trailer URL must not exceed 1000 characters")
    private String trailerUrl;

    private LocalDate releaseDate;

    @Size(max = 255, message = "Genre must not exceed 255 characters")
    private String genre;

    @Size(max = 255, message = "Director must not exceed 255 characters")
    private String director;

    @Size(max = 5000, message = "Cast must not exceed 5000 characters")
    private String cast;

    @Size(max = 10, message = "Rating must not exceed 10 characters")
    private String rating;

    @Pattern(regexp = "^(P|T13|T16|T18)?$", message = "Age rating must be one of: P, T13, T16, T18")
    @Size(max = 10, message = "Age rating must not exceed 10 characters")
    private String ageRating;
}
