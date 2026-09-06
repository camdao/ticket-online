package com.ticket_online.domain.movies.dao;

import com.ticket_online.domain.movies.domain.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, MovieRepositoryCustom {
    // Todo:
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
