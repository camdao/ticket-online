package com.ticket_online.domain.movies.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ticket_online.domain.movies.dao.MovieRepository;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock private MovieRepository movieRepository;

    @InjectMocks private MovieService movieService;

    @Test
    @DisplayName("Should return movie by id when movie exists")
    void shouldReturnMovieByIdWhenMovieExists() {
        // Given
        Long movieId = 1L;
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
                        "T13");
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

        // When
        MovieResponse result = movieService.getMovieById(movieId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Avatar 2");
        assertThat(result.duration()).isEqualTo(192);
        assertThat(result.status()).isEqualTo(MovieStatus.NOW_SHOWING);
    }

    @Test
    @DisplayName("Should throw exception when movie not found")
    void shouldThrowExceptionWhenMovieNotFound() {
        // Given
        Long movieId = 999L;
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> movieService.getMovieById(movieId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MOVIE_NOT_FOUND);
    }

    @Test
    @DisplayName("Should get now showing movies")
    void shouldGetNowShowingMovies() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Movie> movies =
                Arrays.asList(
                        Movie.createMovie(
                                "Movie 1",
                                120,
                                "Desc",
                                "poster.jpg",
                                "trailer.mp4",
                                LocalDate.now(),
                                "Action",
                                "Director",
                                "Cast",
                                "T13"),
                        Movie.createMovie(
                                "Movie 2",
                                150,
                                "Desc",
                                "poster.jpg",
                                "trailer.mp4",
                                LocalDate.now().minusDays(5),
                                "Drama",
                                "Director",
                                "Cast",
                                "T16"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findNowShowingMovies(any(LocalDate.class), eq(pageable)))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.getNowShowingMovies(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).allMatch(movie -> movie.status() == MovieStatus.NOW_SHOWING);
    }

    @Test
    @DisplayName("Should get upcoming movies")
    void shouldGetUpcomingMovies() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Movie> movies =
                Arrays.asList(
                        Movie.createMovie(
                                "Future Movie 1",
                                120,
                                "Desc",
                                "poster.jpg",
                                "trailer.mp4",
                                LocalDate.now().plusDays(10),
                                "Action",
                                "Director",
                                "Cast",
                                "T13"),
                        Movie.createMovie(
                                "Future Movie 2",
                                150,
                                "Desc",
                                "poster.jpg",
                                "trailer.mp4",
                                LocalDate.now().plusDays(20),
                                "Drama",
                                "Director",
                                "Cast",
                                "T16"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findUpcomingMovies(any(LocalDate.class), eq(pageable)))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.getUpcomingMovies(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).allMatch(movie -> movie.status() == MovieStatus.UPCOMING);
    }

    @Test
    @DisplayName("Should get all movies with status filter")
    void shouldGetAllMoviesWithStatusFilter() {
        // Given
        MovieStatus status = MovieStatus.NOW_SHOWING;
        Pageable pageable = PageRequest.of(0, 20);
        List<Movie> movies =
                Arrays.asList(
                        Movie.createMovie(
                                "Movie 1",
                                120,
                                "Desc",
                                "poster.jpg",
                                "trailer.mp4",
                                LocalDate.now(),
                                "Action",
                                "Director",
                                "Cast",
                                "T13"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.getAllMoviesWithFilters(
                        any(), eq("NOW_SHOWING"), eq(null), eq(pageable)))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.getAllMoviesWithFilters(status, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).status()).isEqualTo(MovieStatus.NOW_SHOWING);
    }

    @Test
    @DisplayName("Should search movies by keyword")
    void shouldSearchMoviesByKeyword() {
        // Given
        String keyword = "Avatar";
        Pageable pageable = PageRequest.of(0, 20);
        List<Movie> movies =
                Arrays.asList(
                        Movie.createMovie(
                                "Avatar 2",
                                192,
                                "Desc",
                                "poster.jpg",
                                "trailer.mp4",
                                LocalDate.now(),
                                "Sci-Fi",
                                "James Cameron",
                                "Cast",
                                "T13"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findByTitleContainingIgnoreCase(keyword, pageable))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.searchMoviesByKeyword(keyword, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).contains("Avatar");
    }
}
