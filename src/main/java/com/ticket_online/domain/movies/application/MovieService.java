package com.ticket_online.domain.movies.application;

import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.cinemas.dto.response.CinemaResponse;
import com.ticket_online.domain.movies.dao.MovieRepository;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieResponse;
import com.ticket_online.domain.showtimes.dao.ShowtimeRepository;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    public MovieResponse getMovieById(Long id) {
        Movie movie =
                movieRepository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.MOVIE_NOT_FOUND));
        return MovieResponse.from(movie);
    }

    public MovieListResponse getAllMovies(MovieStatus status, Long cinemaId, Sort sort) {
        List<Movie> movies;

        if (cinemaId != null) {
            // Get movies showing at the specified cinema
            movies = showtimeRepository.findMoviesByCinemaId(cinemaId);

            // Apply status filter if provided
            if (status != null) {
                LocalDate today = LocalDate.now();
                movies =
                        movies.stream()
                                .filter(
                                        movie ->
                                                status == MovieStatus.NOW_SHOWING
                                                        ? movie.isNowShowing()
                                                        : movie.isUpcoming())
                                .collect(Collectors.toList());
            }

            // Apply sorting in memory since we can't use Sort with DISTINCT movie query
            movies = applySorting(movies, sort);
        } else {
            // Existing logic when no cinema filter
            if (status == null) {
                movies = movieRepository.findAll(sort);
            } else {
                LocalDate today = LocalDate.now();

                movies =
                        switch (status) {
                            case NOW_SHOWING ->
                                    movieRepository.findByReleaseDateLessThanEqual(today, sort);
                            case UPCOMING -> movieRepository.findByReleaseDateAfter(today, sort);
                        };
            }
        }

        // Build response with cinemas for each movie
        List<MovieResponse> movieResponses =
                movies.stream()
                        .map(
                                movie -> {
                                    List<Cinema> cinemas =
                                            showtimeRepository.findCinemasByMovieId(movie.getId());
                                    List<CinemaResponse> cinemaResponses =
                                            cinemas.stream()
                                                    .map(CinemaResponse::from)
                                                    .collect(Collectors.toList());
                                    return MovieResponse.from(movie, cinemaResponses);
                                })
                        .collect(Collectors.toList());

        return new MovieListResponse(movieResponses, movieResponses.size());
    }

    public MovieListResponse searchMoviesByKeyword(String keyword) {
        List<Movie> movies = movieRepository.findByTitleContainingIgnoreCase(keyword);
        return MovieListResponse.of(movies);
    }

    private List<Movie> applySorting(List<Movie> movies, Sort sort) {
        if (sort.isUnsorted()) {
            return movies;
        }

        return movies.stream()
                .sorted(
                        (m1, m2) -> {
                            for (Sort.Order order : sort) {
                                int comparison = 0;
                                switch (order.getProperty()) {
                                    case "releaseDate":
                                        comparison =
                                                compareNullable(
                                                        m1.getReleaseDate(), m2.getReleaseDate());
                                        break;
                                    case "title":
                                        comparison = compareNullable(m1.getTitle(), m2.getTitle());
                                        break;
                                    case "duration":
                                        comparison =
                                                compareNullable(m1.getDuration(), m2.getDuration());
                                        break;
                                    default:
                                        // Default to releaseDate if unknown field
                                        comparison =
                                                compareNullable(
                                                        m1.getReleaseDate(), m2.getReleaseDate());
                                }

                                if (comparison != 0) {
                                    return order.isAscending() ? comparison : -comparison;
                                }
                            }
                            return 0;
                        })
                .collect(Collectors.toList());
    }

    private <T extends Comparable<T>> int compareNullable(T a, T b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }
}
