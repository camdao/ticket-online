package com.ticket_online.domain.showtimes.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.cinemas.domain.Screen;
import com.ticket_online.domain.movies.domain.Movie;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Showtime Domain Tests")
class ShowtimeTest {

    @Test
    @DisplayName("Should create showtime with valid data")
    void shouldCreateShowtimeWithValidData() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Avatar",
                        192,
                        "Description",
                        "Action",
                        "James Cameron",
                        LocalDate.of(2023, 12, 16),
                        "https://poster.jpg",
                        "https://trailer.com",
                        "T13",
                        "Sam Worthington",
                        "https://banner.jpg");

        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom",
                        "CGV",
                        "logo.png",
                        "Address",
                        "District",
                        "City",
                        "Phone",
                        "website",
                        "desc");

        Screen screen = Screen.createScreen(1L, "Screen 1", 100, "IMAX");

        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 14, 30);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 17, 30);
        BigDecimal basePrice = BigDecimal.valueOf(85000);

        // When
        Showtime showtime =
                Showtime.createShowtime(movie, cinema, screen, startTime, endTime, basePrice);

        // Then
        assertThat(showtime).isNotNull();
        assertThat(showtime.getMovie()).isEqualTo(movie);
        assertThat(showtime.getCinema()).isEqualTo(cinema);
        assertThat(showtime.getScreen()).isEqualTo(screen);
        assertThat(showtime.getStartTime()).isEqualTo(startTime);
        assertThat(showtime.getEndTime()).isEqualTo(endTime);
        assertThat(showtime.getBasePrice()).isEqualByComparingTo(basePrice);
        assertThat(showtime.getStatus()).isEqualTo(ShowtimeStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should default to ACTIVE status when created")
    void shouldDefaultToActiveStatus() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Avatar",
                        192,
                        "Description",
                        "Action",
                        "James Cameron",
                        LocalDate.of(2023, 12, 16),
                        "https://poster.jpg",
                        "https://trailer.com",
                        "T13",
                        "Sam Worthington",
                        "https://banner.jpg");

        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom",
                        "CGV",
                        "logo.png",
                        "Address",
                        "District",
                        "City",
                        "Phone",
                        "website",
                        "desc");

        Screen screen = Screen.createScreen(1L, "Screen 1", 100, "IMAX");

        // When
        Showtime showtime =
                Showtime.createShowtime(
                        movie,
                        cinema,
                        screen,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(2),
                        BigDecimal.valueOf(85000));

        // Then
        assertThat(showtime.getStatus()).isEqualTo(ShowtimeStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should update showtime status")
    void shouldUpdateShowtimeStatus() {
        // Given
        Movie movie =
                Movie.createMovie(
                        "Avatar",
                        192,
                        "Description",
                        "Action",
                        "James Cameron",
                        LocalDate.of(2023, 12, 16),
                        "https://poster.jpg",
                        "https://trailer.com",
                        "T13",
                        "Sam Worthington",
                        "https://banner.jpg");

        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom",
                        "CGV",
                        "logo.png",
                        "Address",
                        "District",
                        "City",
                        "Phone",
                        "website",
                        "desc");

        Screen screen = Screen.createScreen(1L, "Screen 1", 100, "IMAX");

        Showtime showtime =
                Showtime.createShowtime(
                        movie,
                        cinema,
                        screen,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(2),
                        BigDecimal.valueOf(85000));

        // When
        showtime.updateStatus(ShowtimeStatus.CANCELLED);

        // Then
        assertThat(showtime.getStatus()).isEqualTo(ShowtimeStatus.CANCELLED);
    }
}
