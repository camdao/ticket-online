package com.ticket_online.domain.seats.dao;

import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.seats.domain.SeatType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query(
            "SELECT s FROM Seat s WHERE s.room.id = :roomId AND s.isActive = true ORDER BY s.row, s.number")
    List<Seat> findByRoomId(@Param("roomId") Long roomId);

    @Query(
            "SELECT s FROM Seat s WHERE s.room.id = :roomId AND s.isActive = true ORDER BY s.row, s.number")
    List<Seat> findActiveByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT s FROM Seat s WHERE s.room.id = :roomId AND s.row = :row AND s.number = :number")
    Optional<Seat> findByRoomIdAndRowAndNumber(
            @Param("roomId") Long roomId,
            @Param("row") String row,
            @Param("number") Integer number);

    @Query(
            "SELECT s FROM Seat s WHERE s.room.id = :roomId AND s.type = :type AND s.isActive = true")
    List<Seat> findByRoomIdAndType(@Param("roomId") Long roomId, @Param("type") SeatType type);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.room.id = :roomId AND s.isActive = true")
    Long countByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.isActive = true")
    List<Seat> findByIdIn(@Param("seatIds") List<Long> seatIds);

    @Query(
            "SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Seat s "
                    + "WHERE s.room.id = :roomId AND s.row = :row AND s.number = :number")
    boolean existsByRoomIdAndRowAndNumber(
            @Param("roomId") Long roomId,
            @Param("row") String row,
            @Param("number") Integer number);
}
