package com.ticket_online.domain.movies.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "movies")
public class Movie extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false)
    private Integer duration;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "poster_url", length = 1000)
    private String posterUrl;

    @Column(name = "trailer_url", length = 1000)
    private String trailerUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(length = 255)
    private String genre;

    @Column(length = 255)
    private String director;

    @Column(columnDefinition = "TEXT")
    private String cast;

    @Column(length = 10)
    private String rating;

    @Column(name = "age_rating", length = 10)
    private String ageRating;

    @Builder(access = AccessLevel.PRIVATE)
    Movie(
            String title,
            Integer duration,
            String description,
            String posterUrl,
            String trailerUrl,
            LocalDate releaseDate,
            String genre,
            String director,
            String cast,
            String rating,
            String ageRating) {
        this.title = title;
        this.duration = duration;
        this.description = description;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.director = director;
        this.cast = cast;
        this.rating = rating;
        this.ageRating = ageRating;
    }

    public static Movie createMovie(
            String title,
            Integer duration,
            String description,
            String posterUrl,
            String trailerUrl,
            LocalDate releaseDate,
            String genre,
            String director,
            String cast,
            String rating,
            String ageRating) {
        return Movie.builder()
                .title(title)
                .duration(duration)
                .description(description)
                .posterUrl(posterUrl)
                .trailerUrl(trailerUrl)
                .releaseDate(releaseDate)
                .genre(genre)
                .director(director)
                .cast(cast)
                .rating(rating)
                .ageRating(ageRating)
                .build();
    }

    public void updateMovie(
            String title,
            Integer duration,
            String description,
            String posterUrl,
            String trailerUrl,
            LocalDate releaseDate,
            String genre,
            String director,
            String cast,
            String rating,
            String ageRating) {
        this.title = title;
        this.duration = duration;
        this.description = description;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.director = director;
        this.cast = cast;
        this.rating = rating;
        this.ageRating = ageRating;
    }

    public boolean isUpcoming() {
        return releaseDate != null && releaseDate.isAfter(LocalDate.now());
    }

    public boolean isNowShowing() {
        return releaseDate != null && !releaseDate.isAfter(LocalDate.now());
    }

    public MovieStatus getStatus() {
        if (releaseDate == null) {
            return MovieStatus.UPCOMING;
        }
        LocalDate now = LocalDate.now();
        if (releaseDate.isAfter(now)) {
            return MovieStatus.UPCOMING;
        } else {
            return MovieStatus.NOW_SHOWING;
        }
    }
}
