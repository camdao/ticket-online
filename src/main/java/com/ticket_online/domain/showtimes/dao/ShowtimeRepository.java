package com.ticket_online.domain.showtimes.dao;

import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.domain.ShowtimeStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowtimeRepository
        extends JpaRepository<Showtime, Long>, JpaSpecificationExecutor<Showtime> {

    @Query(
            "SELECT s FROM Showtime s "
                    + "JOIN FETCH s.movie m "
                    + "JOIN FETCH s.cinema c "
                    + "WHERE s.id = :id")
    java.util.Optional<Showtime> findByIdWithDetails(@Param("id") Long id);

    @Query(
            "SELECT s FROM Showtime s "
                    + "JOIN FETCH s.movie m "
                    + "JOIN FETCH s.cinema c "
                    + "WHERE s.movie.id = :movieId "
                    + "AND s.status = :status "
                    + "AND s.startTime >= :startTime "
                    + "ORDER BY s.startTime ASC")
    Page<Showtime> findByMovieIdAndStatusAndStartTimeAfter(
            @Param("movieId") Long movieId,
            @Param("status") ShowtimeStatus status,
            @Param("startTime") LocalDateTime startTime,
            Pageable pageable);

    @Query(
            "SELECT s FROM Showtime s "
                    + "JOIN FETCH s.movie m "
                    + "JOIN FETCH s.cinema c "
                    + "WHERE s.cinema.id = :cinemaId "
                    + "AND s.status = :status "
                    + "AND s.startTime >= :startTime "
                    + "ORDER BY s.startTime ASC")
    Page<Showtime> findByCinemaIdAndStatusAndStartTimeAfter(
            @Param("cinemaId") Long cinemaId,
            @Param("status") ShowtimeStatus status,
            @Param("startTime") LocalDateTime startTime,
            Pageable pageable);
}
