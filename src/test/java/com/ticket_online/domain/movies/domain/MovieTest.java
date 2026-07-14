package com.ticket_online.domain.movies.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MovieTest {

    @Test
    @DisplayName("Should return NOW_SHOWING status when release date is today")
    void shouldReturnNowShowingStatusWhenReleaseDateIsToday() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Avatar 2",
                        192,
                        "Description",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now(),
                        "Action",
                        "James Cameron",
                        "Sam Worthington",
                        "8.5",
                        "T13");

        // When
        MovieStatus status = movie.getStatus();

        // Then
        assertThat(status).isEqualTo(MovieStatus.NOW_SHOWING);
    }

    @Test
    @DisplayName("Should return NOW_SHOWING status when release date is in the past")
    void shouldReturnNowShowingStatusWhenReleaseDateIsInPast() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Titanic",
                        195,
                        "Description",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now().minusDays(30),
                        "Drama, Romance",
                        "James Cameron",
                        "Leonardo DiCaprio",
                        "7.9",
                        "T13");

        // When
        MovieStatus status = movie.getStatus();

        // Then
        assertThat(status).isEqualTo(MovieStatus.NOW_SHOWING);
    }

    @Test
    @DisplayName("Should return UPCOMING status when release date is in the future")
    void shouldReturnUpcomingStatusWhenReleaseDateIsInFuture() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Avengers 5",
                        180,
                        "Description",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now().plusDays(30),
                        "Action, Sci-Fi",
                        "Russo Brothers",
                        "Robert Downey Jr.",
                        "8.0",
                        "T13");

        // When
        MovieStatus status = movie.getStatus();

        // Then
        assertThat(status).isEqualTo(MovieStatus.UPCOMING);
    }

    @Test
    @DisplayName("Should return UPCOMING status when release date is null")
    void shouldReturnUpcomingStatusWhenReleaseDateIsNull() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Unknown Movie",
                        120,
                        "Description",
                        "poster.jpg",
                        "trailer.mp4",
                        null,
                        "Drama",
                        "Unknown",
                        "Unknown Cast",
                        "7.0",
                        "P");

        // When
        MovieStatus status = movie.getStatus();

        // Then
        assertThat(status).isEqualTo(MovieStatus.UPCOMING);
    }

    @Test
    @DisplayName("Should create movie with all fields")
    void shouldCreateMovieWithAllFields() {
        // Given
        String title = "Test Movie";
        Integer duration = 120;
        String description = "Test Description";
        String posterUrl = "poster.jpg";
        String trailerUrl = "trailer.mp4";
        LocalDate releaseDate = LocalDate.now();
        String genre = "Action";
        String director = "Test Director";
        String cast = "Test Cast";
        String rating = "8.0";
        String ageRating = "T13";

        // When
        Movie movie =
                Movie.createMovie(
                        title,
                        duration,
                        description,
                        posterUrl,
                        trailerUrl,
                        releaseDate,
                        genre,
                        director,
                        cast,
                        rating,
                        ageRating);

        // Then
        assertThat(movie.getTitle()).isEqualTo(title);
        assertThat(movie.getDuration()).isEqualTo(duration);
        assertThat(movie.getDescription()).isEqualTo(description);
        assertThat(movie.getPosterUrl()).isEqualTo(posterUrl);
        assertThat(movie.getTrailerUrl()).isEqualTo(trailerUrl);
        assertThat(movie.getReleaseDate()).isEqualTo(releaseDate);
        assertThat(movie.getGenre()).isEqualTo(genre);
        assertThat(movie.getDirector()).isEqualTo(director);
        assertThat(movie.getCast()).isEqualTo(cast);
        assertThat(movie.getRating()).isEqualTo(rating);
        assertThat(movie.getAgeRating()).isEqualTo(ageRating);
    }

    @Test
    @DisplayName("Should update movie fields")
    void shouldUpdateMovieFields() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Original Title",
                        120,
                        "Original Description",
                        "original_poster.jpg",
                        "original_trailer.mp4",
                        LocalDate.now(),
                        "Drama",
                        "Original Director",
                        "Original Cast",
                        "7.0",
                        "P");

        String newTitle = "Updated Title";
        Integer newDuration = 150;
        String newDescription = "Updated Description";
        String newPosterUrl = "updated_poster.jpg";
        String newTrailerUrl = "updated_trailer.mp4";
        LocalDate newReleaseDate = LocalDate.now().plusDays(10);
        String newGenre = "Action";
        String newDirector = "Updated Director";
        String newCast = "Updated Cast";
        String newRating = "8.5";
        String newAgeRating = "T13";

        // When
        movie.updateMovie(
                newTitle,
                newDuration,
                newDescription,
                newPosterUrl,
                newTrailerUrl,
                newReleaseDate,
                newGenre,
                newDirector,
                newCast,
                newRating,
                newAgeRating);

        // Then
        assertThat(movie.getTitle()).isEqualTo(newTitle);
        assertThat(movie.getDuration()).isEqualTo(newDuration);
        assertThat(movie.getDescription()).isEqualTo(newDescription);
        assertThat(movie.getPosterUrl()).isEqualTo(newPosterUrl);
        assertThat(movie.getTrailerUrl()).isEqualTo(newTrailerUrl);
        assertThat(movie.getReleaseDate()).isEqualTo(newReleaseDate);
        assertThat(movie.getGenre()).isEqualTo(newGenre);
        assertThat(movie.getDirector()).isEqualTo(newDirector);
        assertThat(movie.getCast()).isEqualTo(newCast);
        assertThat(movie.getRating()).isEqualTo(newRating);
        assertThat(movie.getAgeRating()).isEqualTo(newAgeRating);
    }

    @Test
    @DisplayName("Should return true for isNowShowing when release date is today or past")
    void shouldReturnTrueForIsNowShowingWhenReleaseDateIsTodayOrPast() {
        // Given
        Movie movieToday =
                Movie.createMovie(
                        "Movie Today",
                        120,
                        "Desc",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now(),
                        "Action",
                        "Director",
                        "Cast",
                        "8.0",
                        "T13");

        Movie moviePast =
                Movie.createMovie(
                        "Movie Past",
                        120,
                        "Desc",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now().minusDays(1),
                        "Action",
                        "Director",
                        "Cast",
                        "8.0",
                        "T13");

        // When & Then
        assertThat(movieToday.isNowShowing()).isTrue();
        assertThat(moviePast.isNowShowing()).isTrue();
    }

    @Test
    @DisplayName("Should return false for isNowShowing when release date is in future")
    void shouldReturnFalseForIsNowShowingWhenReleaseDateIsInFuture() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Future Movie",
                        120,
                        "Desc",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now().plusDays(1),
                        "Action",
                        "Director",
                        "Cast",
                        "8.0",
                        "T13");

        // When & Then
        assertThat(movie.isNowShowing()).isFalse();
    }

    @Test
    @DisplayName("Should return true for isUpcoming when release date is in future")
    void shouldReturnTrueForIsUpcomingWhenReleaseDateIsInFuture() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Upcoming Movie",
                        120,
                        "Desc",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now().plusDays(10),
                        "Action",
                        "Director",
                        "Cast",
                        "8.0",
                        "T13");

        // When & Then
        assertThat(movie.isUpcoming()).isTrue();
    }

    @Test
    @DisplayName("Should return false for isUpcoming when release date is today or past")
    void shouldReturnFalseForIsUpcomingWhenReleaseDateIsTodayOrPast() {
        // Given
        Movie movieToday =
                Movie.createMovie(
                        "Movie Today",
                        120,
                        "Desc",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now(),
                        "Action",
                        "Director",
                        "Cast",
                        "8.0",
                        "T13");

        Movie moviePast =
                Movie.createMovie(
                        "Movie Past",
                        120,
                        "Desc",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now().minusDays(1),
                        "Action",
                        "Director",
                        "Cast",
                        "8.0",
                        "T13");

        // When & Then
        assertThat(movieToday.isUpcoming()).isFalse();
        assertThat(moviePast.isUpcoming()).isFalse();
    }
}