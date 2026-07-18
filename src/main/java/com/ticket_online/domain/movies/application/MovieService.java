package com.ticket_online.domain.movies.application;

import com.ticket_online.domain.cinemas.dto.response.ShowtimeResponse;
import com.ticket_online.domain.movies.dao.MovieRepository;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieResponse;
import com.ticket_online.domain.showtimes.application.ShowtimeService;
import com.ticket_online.domain.showtimes.domain.ShowtimeStatus;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
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
    private final ShowtimeService showtimeService;

    public MovieResponse getMovieById(Long id) {
        Movie movie =
                movieRepository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.MOVIE_NOT_FOUND));
        return MovieResponse.from(movie);
    }

    public MovieListResponse getUpcomingMovies(Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findUpcomingMovies(LocalDate.now(), pageable);
        return MovieListResponse.of(moviePage);
    }

    public MovieListResponse getNowShowingMovies(Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findNowShowingMovies(LocalDate.now(), pageable);
        return MovieListResponse.of(moviePage);
    }

    public MovieListResponse getAllMoviesWithFilters(
            MovieStatus status, String genre, Pageable pageable) {

        Page<Movie> moviePage =
                movieRepository.getAllMoviesWithFilters(
                        ShowtimeStatus.ACTIVE,
                        status != null ? status.name() : null,
                        genre,
                        pageable);

        return MovieListResponse.of(moviePage);
    }

    public MovieListResponse searchMoviesByKeyword(String keyword, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        return MovieListResponse.of(moviePage);
    }

    public List<ShowtimeResponse> getMovieShowtimes(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate) {
        movieRepository
                .findById(movieId)
                .orElseThrow(() -> new CustomException(ErrorCode.MOVIE_NOT_FOUND));
        return showtimeService.getShowtimesByMovieId(
                movieId, cinemaId, city, date, startDate, endDate);
    }
}
