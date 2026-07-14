package com.ticket_online.domain.showtimes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.cinemas.domain.Screen;
import com.ticket_online.domain.movies.domain.Movie;
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
import org.springframework.test.util.ReflectionTestUtils;

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
        // Create test movie with correct signature
        movie =
                Movie.createMovie(
                        "Avatar: The Way of Water",
                        192,
                        "Jake Sully lives with his newfound family...",
                        "https://cdn.example.com/avatar2.jpg",
                        "https://youtube.com/watch?v=...",
                        LocalDate.of(2023, 12, 16),
                        "Action, Adventure, Sci-Fi",
                        "James Cameron",
                        "Sam Worthington, Zoe Saldana",
                        "8.5",
                        "T13");
        ReflectionTestUtils.setField(movie, "id", 1L);

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
        ReflectionTestUtils.setField(cinema, "id", 5L);

        // Create test screen
        screen = Screen.createScreen(5L, "Screen 3", 120, "IMAX");
        ReflectionTestUtils.setField(screen, "id", 12L);

        // Create test showtime with correct signature
        showtime =
                Showtime.createShowtime(
                        movie,
                        cinema,
                        screen,
                        LocalDateTime.of(2024, 1, 15, 14, 30),
                        LocalDateTime.of(2024, 1, 15, 17, 42),
                        BigDecimal.valueOf(85000));
        ReflectionTestUtils.setField(showtime, "id", 101L);
    }

    @Nested
    @DisplayName("getShowtimeById Tests")
    class GetShowtimeByIdTests {

        @Test
        @DisplayName("Should return showtime detail when showtime exists")
        void shouldReturnShowtimeDetailWhenExists() {
            // Given
            when(showtimeRepository.findByIdWithDetails(101L)).thenReturn(Optional.of(showtime));

            // When
            ShowtimeDetailResponse result = showtimeService.getShowtimeById(101L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(101L);
            assertThat(result.movie()).isNotNull();
            assertThat(result.movie().id()).isEqualTo(1L);
            assertThat(result.movie().title()).isEqualTo("Avatar: The Way of Water");
            assertThat(result.cinema()).isNotNull();
            assertThat(result.cinema().id()).isEqualTo(5L);
            assertThat(result.cinema().name()).isEqualTo("CGV Vincom Center");
            assertThat(result.screen()).isNotNull();
            assertThat(result.screen().id()).isEqualTo(12L);
            assertThat(result.screen().name()).isEqualTo("Screen 3");
            assertThat(result.startTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30));
            assertThat(result.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(85000));
            assertThat(result.status()).isEqualTo(ShowtimeStatus.ACTIVE);

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
            assertThat(result.content()).hasSize(1);
            assertThat(result.page()).isEqualTo(0);
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.totalPages()).isEqualTo(1);

            ShowtimeResponse showtimeResponse = result.content().get(0);
            assertThat(showtimeResponse.id()).isEqualTo(101L);
            assertThat(showtimeResponse.movieTitle()).isEqualTo("Avatar: The Way of Water");
            assertThat(showtimeResponse.cinemaName()).isEqualTo("CGV Vincom Center");

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
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).movieId()).isEqualTo(1L);

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
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).cinemaId()).isEqualTo(5L);

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
            assertThat(result.content()).hasSize(1);

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
            assertThat(result.content()).hasSize(1);

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
            assertThat(result.content()).hasSize(1);

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
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).movieId()).isEqualTo(1L);
            assertThat(result.content().get(0).cinemaId()).isEqualTo(5L);

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
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isEqualTo(0);

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
            List<Showtime> showtimes = List.of(showtime);

            when(showtimeRepository.findAll(any(Specification.class)))
                    .thenReturn(showtimes);

            // When
            List<ShowtimeResponse> result =
                    showtimeService.getShowtimesByMovieId(1L, null, null, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).movieId()).isEqualTo(1L);

            verify(showtimeRepository).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("Should filter movie showtimes by cinema")
        void shouldFilterMovieShowtimesByCinema() {
            // Given
            List<Showtime> showtimes = List.of(showtime);

            when(showtimeRepository.findAll(any(Specification.class)))
                    .thenReturn(showtimes);

            // When
            List<ShowtimeResponse> result =
                    showtimeService.getShowtimesByMovieId(1L, 5L, null, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).movieId()).isEqualTo(1L);
            assertThat(result.get(0).cinemaId()).isEqualTo(5L);

            verify(showtimeRepository).findAll(any(Specification.class));
        }
    }

    @Nested
    @DisplayName("getShowtimesByCinemaId Tests")
    class GetShowtimesByCinemaIdTests {

        @Test
        @DisplayName("Should return showtimes for specific cinema")
        void shouldReturnShowtimesForCinema() {
            // Given
            List<Showtime> showtimes = List.of(showtime);

            when(showtimeRepository.findAll(any(Specification.class)))
                    .thenReturn(showtimes);

            // When
            List<ShowtimeResponse> result =
                    showtimeService.getShowtimesByCinemaId(5L, null, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).cinemaId()).isEqualTo(5L);

            verify(showtimeRepository).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("Should filter cinema showtimes by movie")
        void shouldFilterCinemaShowtimesByMovie() {
            // Given
            List<Showtime> showtimes = List.of(showtime);

            when(showtimeRepository.findAll(any(Specification.class)))
                    .thenReturn(showtimes);

            // When
            List<ShowtimeResponse> result =
                    showtimeService.getShowtimesByCinemaId(5L, 1L, null, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).cinemaId()).isEqualTo(5L);
            assertThat(result.get(0).movieId()).isEqualTo(1L);

            verify(showtimeRepository).findAll(any(Specification.class));
        }
    }
}