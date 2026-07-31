package com.ticket_online.domain.movies.dao;

import com.ticket_online.domain.movies.domain.Movie;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByReleaseDateLessThanEqual(LocalDate date, Sort sort);

    List<Movie> findByReleaseDateAfter(LocalDate date, Sort sort);

    List<Movie> findByTitleContainingIgnoreCase(String title);
}
