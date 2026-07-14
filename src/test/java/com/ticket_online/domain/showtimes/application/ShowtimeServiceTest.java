package com.ticket_online.domain.showtimes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.cinemas.domain.Screen;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import com.ticket_online.domain.showtimes.dao.ShowtimeRepository;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.domain.ShowtimeStatus;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeDetailResponse;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeListResponse;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShowtimeService Unit Tests")
class ShowtimeServiceTest {

    @Mock private ShowtimeRepository showtimeRepository;

    @InjectMocks private ShowtimeService showtimeService;

    private Movie movie;
    private Cinema cinema;
    private Screen screen;
    private Showtime showtime;

    @BeforeEach
    void setUp() {
        // Create test movie
        movie =
                Movie.createMovie(
                        "Avatar: The Way of Water",
                        192,
                        "Jake Sully lives with his newfound family...",
                        "Action, Adventure, Sci-Fi",
                        "James Cameron",
                        "Sam Worthington, Zoe Saldana",
                        8.5,
                        "T13",
                        LocalDate.of(2023, 12, 16),
                        "https://cdn.example.com/avatar2.jpg",
                        "https://youtube.com/watch?v=...",
                        MovieStatus.NOW_SHOWING);
        movie.setId(1L);

        // Create test cinema
        cinema =
                Cinema.createCinema(
                        "CGV Vincom Center",
                        "CGV",
                        "https://cdn.example.com/cgv-logo.png",
                        "72 Lê Thánh Tôn",
                        "Quận 1",
                        "TP. Hồ Chí Minh",
                        "1900xxxx",
                        "https://cgv.vn",
                        "CGV Vincom Center là rạp chiếu phim hiện đại...");
        cinema.setId(5L);

        // Create test screen
        screen = Screen.createScreen(5L, "Screen 3", 120, "IMAX");
        screen.setId(12L);

        // Create test showtime
        showtime =
                Showtime.createShowtime(
                        1L,
                        5L,
                        12L,
                        LocalDateTime.of(2024, 1, 15, 14, 30),
                        LocalDateTime.of(2024, 1, 15, 17, 42),
                        BigDecimal.valueOf(85000),
                        ShowtimeStatus.ACTIVE);
        showtime.setId(101L);
        showtime.setMovie(movie);
        showtime.setCinema(cinema);
        showtime.setScreen(screen);
    }

    @Nested
    @DisplayName("getShowtimeById Tests")
    class GetShowtimeByIdTests {

        @Test
        @DisplayName("Should return showtime detail when showtime exists")
        void shouldReturnShowtimeDetailWhenExists() {
            // Given
            when(showtimeRepository.findByIdWithDetails(101L))
                    .thenReturn(Optional.of(showtime));

            // When
            ShowtimeDetailResponse result = showtimeService.getShowtimeById(101L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(101L);
            assertThat(result.getMovie()).isNotNull();
            assertThat(result.getMovie().getId()).isEqualTo(1L);
            assertThat(result.getMovie().getTitle()).isEqualTo("Avatar: The Way of Water");
            assertThat(result.getCinema()).isNotNull();
            assertThat(result.getCinema().getId()).isEqualTo(5L);
            assertThat(result.getCinema().getName()).isEqualTo("CGV Vincom Center");
            assertThat(result.getScreen()).isNotNull();
            assertThat(result.getScreen().getId()).isEqualTo(12L);
            assertThat(result.getScreen().getName()).isEqualTo("Screen 3");
            assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30));
            assertThat(result.getBasePrice()).isEqualByComparingTo(BigDecimal.valueOf(85000));
            assertThat(result.getStatus()).isEqualTo(ShowtimeStatus.ACTIVE);

            verify(showtimeRepository).findByIdWithDetails(101L);
        }

        @Test
        @DisplayName("Should throw CustomException when showtime not found")
        void shouldThrowExceptionWhenShowtimeNotFound() {
            // Given
            when(showtimeRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> showtimeService.getShowtimeById(999L))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOWTIME_NOT_FOUND);

            verify(showtimeRepository).findByIdWithDetails(999L);
        }
    }

    @Nested
    @DisplayName("searchShowtimes Tests")
    class SearchShowtimesTests {

        @Test
        @DisplayName("Should return all showtimes when no filters applied")
        void shouldReturnAllShowtimesWithNoFilters() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            ShowtimeListResponse result =
                    showtimeService.searchShowtimes(null, null, null, null, null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);

            ShowtimeResponse showtimeResponse = result.getContent().get(0);
            assertThat(showtimeResponse.getId()).isEqualTo(101L);
            assertThat(showtimeResponse.getMovieTitle()).isEqualTo("Avatar: The Way of Water");
            assertThat(showtimeResponse.getCinemaName()).isEqualTo("CGV Vincom Center");

            verify(showtimeRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should filter by movieId")
        void shouldFilterByMovieId() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            ShowtimeListResponse result =
                    showtimeService.searchShowtimes(1L, null, null, null, null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMovieId()).isEqualTo(1L);

            verify(showtimeRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should filter by cinemaId")
        void shouldFilterByCinemaId() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            ShowtimeListResponse result =
                    showtimeService.searchShowtimes(null, 5L, null, null, null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCinemaId()).isEqualTo(5L);

            verify(showtimeRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should filter by city")
        void shouldFilterByCity() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            ShowtimeListResponse result =
                    showtimeService.searchShowtimes(
                            null, null, "TP. Hồ Chí Minh", null, null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(showtimeRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should filter by specific date")
        void shouldFilterBySpecificDate() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            ShowtimeListResponse result =
                    showtimeService.searchShowtimes(
                            null, null, null, "2024-01-15", null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(showtimeRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should filter by date range")
        void shouldFilterByDateRange() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            ShowtimeListResponse result =
                    showtimeService.searchShowtimes(
                            null, null, null, null, "2024-01-01", "2024-01-31", pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(showtimeRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should combine multiple filters")
        void shouldCombineMultipleFilters() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            ShowtimeListResponse result =
                    showtimeService.searchShowtimes(
                            1L, 5L, "TP. Hồ Chí Minh", "2024-01-15", null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMovieId()).isEqualTo(1L);
            assertThat(result.getContent().get(0).getCinemaId()).isEqualTo(5L);

            verify(showtimeRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return empty list when no showtimes match filters")
        void shouldReturnEmptyListWhenNoMatches() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Showtime> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(emptyPage);

            // When
            ShowtimeListResponse result =
                    showtimeService.searchShowtimes(999L, null, null, null, null, null, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(showtimeRepository).findAll(any(Specification.class), eq(pageable));
        }
    }

    @Nested
    @DisplayName("getShowtimesByMovieId Tests")
    class GetShowtimesByMovieIdTests {

        @Test
        @DisplayName("Should return showtimes for specific movie")
        void shouldReturnShowtimesForMovie() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            List<ShowtimeResponse> result =
                    showtimeService.getShowtimesByMovieId(1L, null, null, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMovieId()).isEqualTo(1L);

            verify(showtimeRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter movie showtimes by cinema")
        void shouldFilterMovieShowtimesByCinema() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            List<ShowtimeResponse> result =
                    showtimeService.getShowtimesByMovieId(1L, 5L, null, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMovieId()).isEqualTo(1L);
            assertThat(result.get(0).getCinemaId()).isEqualTo(5L);

            verify(showtimeRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getShowtimesByCinemaId Tests")
    class GetShowtimesByCinemaIdTests {

        @Test
        @DisplayName("Should return showtimes for specific cinema")
        void shouldReturnShowtimesForCinema() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            List<ShowtimeResponse> result =
                    showtimeService.getShowtimesByCinemaId(5L, null, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCinemaId()).isEqualTo(5L);

            verify(showtimeRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter cinema showtimes by movie")
        void shouldFilterCinemaShowtimesByMovie() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Showtime> showtimes = List.of(showtime);
            Page<Showtime> showtimePage = new PageImpl<>(showtimes, pageable, 1);

            when(showtimeRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(showtimePage);

            // When
            List<ShowtimeResponse> result =
                    showtimeService.getShowtimesByCinemaId(5L, 1L, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCinemaId()).isEqualTo(5L);
            assertThat(result.get(0).getMovieId()).isEqualTo(1L);

            verify(showtimeRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}