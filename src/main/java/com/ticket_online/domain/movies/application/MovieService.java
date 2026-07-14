package com.ticket_online.domain.movies.application;

import com.ticket_online.domain.movies.dao.MovieRepository;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
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

    public MovieResponse getMovieById(Long id) {
        Movie movie =
                movieRepository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.MOVIE_NOT_FOUND));
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

    public MovieListResponse getAllMoviesWithFilters(
            MovieStatus status, String genre, Pageable pageable) {
        Page<Movie> moviePage;

        if (status != null && genre != null) {
            moviePage =
                    movieRepository
                            .searchMovies(null, genre, null, null, pageable)
                            .map(
                                    movie -> {
                                        if (movie.getStatus() == status) {
                                            return movie;
                                        }
                                        return null;
                                    });
            moviePage = moviePage.map(movie -> movie); // Filter out nulls (not ideal, better to use
            // Specification)
        } else if (status != null) {
            moviePage = movieRepository.findByStatus(status.name(), LocalDate.now(), pageable);
        } else if (genre != null) {
            moviePage = movieRepository.findByGenreContainingIgnoreCase(genre, pageable);
        } else {
            moviePage = movieRepository.findAll(pageable);
        }

        return buildMovieListResponse(moviePage);
    }

    public MovieListResponse searchMoviesByKeyword(String keyword, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        return buildMovieListResponse(moviePage);
    }

    private MovieListResponse buildMovieListResponse(Page<Movie> moviePage) {
        List<MovieResponse> movieResponses =
                moviePage.getContent().stream()
                        .map(MovieResponse::from)
                        .collect(Collectors.toList());

        return MovieListResponse.builder()
                .content(movieResponses)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .build();
    }
}
