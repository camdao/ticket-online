package com.ticket_online.domain.movies.dao;

import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import java.util.List;
import org.springframework.data.domain.Sort;

public interface MovieRepositoryCustom {
    List<Movie> findMovies(MovieStatus status, Long cinemaId, Sort sort);
}
