package com.ticket_online.domain.showtimes.dao;

import com.ticket_online.domain.showtimes.domain.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowtimeRepository
        extends JpaRepository<Showtime, Long>, JpaSpecificationExecutor<Showtime> {

    @Query(
            "SELECT s FROM Showtime s "
                    + "JOIN FETCH s.movie m "
                    + "JOIN FETCH s.room r "
                    + "JOIN FETCH r.cinema c "
                    + "WHERE s.id = :id")
    java.util.Optional<Showtime> findByIdWithDetails(@Param("id") Long id);
}
