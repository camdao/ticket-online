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
                        "8.5",
                        "T13");
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

        // When
        MovieResponse result = movieService.getMovieById(movieId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Avatar 2");
        assertThat(result.getDuration()).isEqualTo(192);
        assertThat(result.getStatus()).isEqualTo(MovieStatus.NOW_SHOWING);
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
    @DisplayName("Should return all movies with pagination")
    void shouldReturnAllMoviesWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Movie> movies =
                Arrays.asList(
                        Movie.createMovie(
                                "Movie 1",
                                120,
                                "Desc",
                                "poster1.jpg",
                                "trailer1.mp4",
                                LocalDate.now(),
                                "Action",
                                "Director 1",
                                "Cast 1",
                                "8.0",
                                "T13"),
                        Movie.createMovie(
                                "Movie 2",
                                150,
                                "Desc",
                                "poster2.jpg",
                                "trailer2.mp4",
                                LocalDate.now().plusDays(10),
                                "Drama",
                                "Director 2",
                                "Cast 2",
                                "7.5",
                                "T16"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findAll(pageable)).thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.getAllMovies(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should search movies by title")
    void shouldSearchMoviesByTitle() {
        // Given
        String title = "Avatar";
        Pageable pageable = PageRequest.of(0, 20);
        List<Movie> movies =
                Arrays.asList(
                        Movie.createMovie(
                                "Avatar 1",
                                162,
                                "Desc",
                                "poster.jpg",
                                "trailer.mp4",
                                LocalDate.now().minusYears(1),
                                "Sci-Fi",
                                "James Cameron",
                                "Cast",
                                "7.8",
                                "T13"),
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
                                "8.5",
                                "T13"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findByTitleContainingIgnoreCase(title, pageable))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.searchMoviesByTitle(title, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).contains("Avatar");
        assertThat(result.getContent().get(1).getTitle()).contains("Avatar");
    }

    @Test
    @DisplayName("Should search movies by genre")
    void shouldSearchMoviesByGenre() {
        // Given
        String genre = "Action";
        Pageable pageable = PageRequest.of(0, 20);
        List<Movie> movies =
                Arrays.asList(
                        Movie.createMovie(
                                "Action Movie 1",
                                120,
                                "Desc",
                                "poster.jpg",
                                "trailer.mp4",
                                LocalDate.now(),
                                "Action",
                                "Director",
                                "Cast",
                                "8.0",
                                "T13"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findByGenreContainingIgnoreCase(genre, pageable))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.searchMoviesByGenre(genre, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getGenre()).contains("Action");
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
                                "8.0",
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
                                "7.5",
                                "T16"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findNowShowingMovies(any(LocalDate.class), eq(pageable)))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.getNowShowingMovies(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(movie -> movie.getStatus() == MovieStatus.NOW_SHOWING);
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
                                "8.0",
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
                                "7.5",
                                "T16"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findUpcomingMovies(any(LocalDate.class), eq(pageable)))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.getUpcomingMovies(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(movie -> movie.getStatus() == MovieStatus.UPCOMING);
    }

    @Test
    @DisplayName("Should search movies with multiple criteria")
    void shouldSearchMoviesWithMultipleCriteria() {
        // Given
        String title = "Avatar";
        String genre = "Sci-Fi";
        String rating = "8.5";
        String director = "Cameron";
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
                                "8.5",
                                "T13"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.searchMovies(title, genre, rating, director, pageable))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.searchMovies(title, genre, rating, director, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Avatar");
        assertThat(result.getContent().get(0).getGenre()).contains("Sci-Fi");
        assertThat(result.getContent().get(0).getDirector()).contains("Cameron");
    }

    @Test
    @DisplayName("Should get movies by IDs")
    void shouldGetMoviesByIds() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
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
                                "8.0",
                                "T13"),
                        Movie.createMovie(
                                "Movie 2",
                                150,
                                "Desc",
                                "poster.jpg",
                                "trailer.mp4",
                                LocalDate.now(),
                                "Drama",
                                "Director",
                                "Cast",
                                "7.5",
                                "T16"));
        when(movieRepository.findByIdIn(ids)).thenReturn(movies);

        // When
        List<MovieResponse> result = movieService.getMoviesByIds(ids);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
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
                                "8.0",
                                "T13"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findByStatus(eq("NOW_SHOWING"), any(LocalDate.class), eq(pageable)))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.getAllMoviesWithFilters(status, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(MovieStatus.NOW_SHOWING);
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
                                "8.5",
                                "T13"));
        Page<Movie> moviePage = new PageImpl<>(movies, pageable, movies.size());
        when(movieRepository.findByTitleContainingIgnoreCase(keyword, pageable))
                .thenReturn(moviePage);

        // When
        MovieListResponse result = movieService.searchMoviesByKeyword(keyword, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Avatar");
    }
}