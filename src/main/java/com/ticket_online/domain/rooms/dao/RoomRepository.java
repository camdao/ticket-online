package com.ticket_online.domain.rooms.dao;

import com.ticket_online.domain.rooms.domain.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query(
            "SELECT r.cinemaId, COUNT(r) FROM Room r WHERE r.cinemaId IN :cinemaIds GROUP BY"
                    + " r.cinemaId")
    List<Object[]> countByCinemaIds(@Param("cinemaIds") List<Long> cinemaIds);
}
