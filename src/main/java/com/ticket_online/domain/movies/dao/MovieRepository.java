package com.ticket_online.domain.movies.dao;

import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.showtimes.domain.ShowtimeStatus;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate > :today ORDER BY m.releaseDate ASC")
    Page<Movie> findUpcomingMovies(@Param("today") LocalDate today, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate <= :today ORDER BY m.releaseDate DESC")
    Page<Movie> findNowShowingMovies(@Param("today") LocalDate today, Pageable pageable);

    @Query(
            """
    SELECT m
    FROM Movie m
    WHERE
        (:genre IS NULL OR LOWER(m.genre) LIKE LOWER(CONCAT('%', :genre, '%')))
    AND (
        :status IS NULL
        OR (
            :status = 'UPCOMING'
            AND m.releaseDate > CURRENT_DATE
        )
        OR (
            :status = 'NOW_SHOWING'
            AND EXISTS (
                SELECT 1
                FROM Showtime s
                WHERE s.movie = m
                  AND s.status = :showtimeStatus
                  AND s.endTime > CURRENT_TIMESTAMP
            )
        )
    )
    """)
    Page<Movie> getAllMoviesWithFilters(
            @Param("showtimeStatus") ShowtimeStatus showtimeStatus,
            @Param("status") String status,
            @Param("genre") String genre,
            Pageable pageable);
}
