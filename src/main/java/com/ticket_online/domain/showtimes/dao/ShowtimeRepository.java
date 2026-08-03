package com.ticket_online.domain.showtimes.dao;

import com.ticket_online.domain.showtimes.domain.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowtimeRepository
        extends JpaRepository<Showtime, Long>,
                JpaSpecificationExecutor<Showtime>,
                ShowResponseCustom {

    @Query(
            "SELECT s FROM Showtime s "
                    + "JOIN FETCH s.movie m "
                    + "JOIN FETCH s.room r "
                    + "JOIN FETCH r.cinema c "
                    + "WHERE s.id = :id")
    java.util.Optional<Showtime> findByIdWithDetails(@Param("id") Long id);

    @Query(
            "SELECT DISTINCT c FROM Showtime s "
                    + "JOIN s.room r "
                    + "JOIN r.cinema c "
                    + "WHERE s.movie.id = :movieId")
    java.util.List<com.ticket_online.domain.cinemas.domain.Cinema> findCinemasByMovieId(
            @Param("movieId") Long movieId);

    @Query(
            "SELECT DISTINCT s.movie FROM Showtime s "
                    + "JOIN s.room r "
                    + "WHERE r.cinema.id = :cinemaId")
    java.util.List<com.ticket_online.domain.movies.domain.Movie> findMoviesByCinemaId(
            @Param("cinemaId") Long cinemaId);
}
