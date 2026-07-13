package com.ticket_online.domain.movies.application;

import com.ticket_online.domain.movies.dao.MovieRepository;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieRequest;
import com.ticket_online.domain.movies.dto.MovieResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;

    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        Movie movie =
                Movie.createMovie(
                        request.getTitle(),
                        request.getDuration(),
                        request.getDescription(),
                        request.getImageUrl(),
                        request.getTrailerUrl(),
                        request.getReleaseDate(),
                        request.getGenre(),
                        request.getDirector(),
                        request.getCast(),
                        request.getRating());

        Movie savedMovie = movieRepository.save(movie);
        return MovieResponse.from(savedMovie);
    }

    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie =
                movieRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Movie not found with id: " + id));

        movie.updateMovie(
                request.getTitle(),
                request.getDuration(),
                request.getDescription(),
                request.getImageUrl(),
                request.getTrailerUrl(),
                request.getReleaseDate(),
                request.getGenre(),
                request.getDirector(),
                request.getCast(),
                request.getRating());

        return MovieResponse.from(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new IllegalArgumentException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie =
                movieRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Movie not found with id: " + id));
        return MovieResponse.from(movie);
    }

    public MovieListResponse getAllMovies(Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        return buildMovieListResponse(moviePage);
    }

    public MovieListResponse searchMoviesByTitle(String title, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        return buildMovieListResponse(moviePage);
    }

    public MovieListResponse searchMoviesByGenre(String genre, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByGenreContainingIgnoreCase(genre, pageable);
        return buildMovieListResponse(moviePage);
    }

    public MovieListResponse searchMoviesByRating(String rating, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByRating(rating, pageable);
        return buildMovieListResponse(moviePage);
    }

    public MovieListResponse searchMoviesByDirector(String director, Pageable pageable) {
        Page<Movie> moviePage =
                movieRepository.findByDirectorContainingIgnoreCase(director, pageable);
        return buildMovieListResponse(moviePage);
    }

    public MovieListResponse searchMovies(
            String title, String genre, String rating, String director, Pageable pageable) {
        Page<Movie> moviePage =
                movieRepository.searchMovies(title, genre, rating, director, pageable);
        return buildMovieListResponse(moviePage);
    }

    public MovieListResponse getUpcomingMovies(Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findUpcomingMovies(LocalDate.now(), pageable);
        return buildMovieListResponse(moviePage);
    }

    public MovieListResponse getNowShowingMovies(Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findNowShowingMovies(LocalDate.now(), pageable);
        return buildMovieListResponse(moviePage);
    }

    public MovieListResponse getMoviesByReleaseDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Movie> moviePage =
                movieRepository.findByReleaseDateBetween(startDate, endDate, pageable);
        return buildMovieListResponse(moviePage);
    }

    public List<MovieResponse> getMoviesByIds(List<Long> ids) {
        return movieRepository.findByIdIn(ids).stream()
                .map(MovieResponse::from)
                .collect(Collectors.toList());
    }

    private MovieListResponse buildMovieListResponse(Page<Movie> moviePage) {
        List<MovieResponse> movieResponses =
                moviePage.getContent().stream()
                        .map(MovieResponse::from)
                        .collect(Collectors.toList());

        return MovieListResponse.builder()
                .movies(movieResponses)
                .currentPage(moviePage.getNumber())
                .totalPages(moviePage.getTotalPages())
                .totalElements(moviePage.getTotalElements())
                .pageSize(moviePage.getSize())
                .build();
    }
}
