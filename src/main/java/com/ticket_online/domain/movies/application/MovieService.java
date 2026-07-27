package com.ticket_online.domain.movies.application;

import com.ticket_online.domain.movies.dao.MovieRepository;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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

    public MovieListResponse getAllMovies(MovieStatus status, Sort sort) {
        List<Movie> movies;
        if (status != null) {
            movies = movieRepository.findByStatus(status, sort);
        } else {
            movies = movieRepository.findAll(sort);
        }
        return MovieListResponse.of(movies);
    }

    public MovieListResponse searchMoviesByKeyword(String keyword) {
        List<Movie> movies = movieRepository.findByTitleContainingIgnoreCase(keyword);
        return MovieListResponse.of(movies);
    }
}
