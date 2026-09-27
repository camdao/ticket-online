package com.ticket_online.domain.seats.dao;

import com.ticket_online.domain.seats.domain.Seat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query(
            """
        SELECT s
        FROM Seat s
        WHERE s.room.id = :roomId
          AND s.isActive = true
        ORDER BY s.row, s.number
    """)
    List<Seat> findByRoomId(@Param("roomId") Long roomId);

    @Query(
            """
        SELECT s
        FROM Seat s
        WHERE s.id IN :seatIds
          AND s.isActive = true
        """)
    List<Seat> findByIdIn(@Param("seatIds") List<Long> seatIds);
}
