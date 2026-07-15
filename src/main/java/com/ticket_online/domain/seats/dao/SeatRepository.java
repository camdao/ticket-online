package com.ticket_online.domain.seats.dao;

import com.ticket_online.domain.seats.domain.Seat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByRoomId(Long roomId);

    @Query("SELECT s FROM Seat s WHERE s.room.id = :roomId ORDER BY s.rowCode, s.seatNumber")
    List<Seat> findByRoomIdOrderByPosition(@Param("roomId") Long roomId);

    Optional<Seat> findByRoomIdAndRowCodeAndSeatNumber(
            Long roomId, String rowCode, Integer seatNumber);

    boolean existsByRoomIdAndRowCodeAndSeatNumber(Long roomId, String rowCode, Integer seatNumber);

    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds")
    List<Seat> findAllByIdIn(@Param("seatIds") List<Long> seatIds);

    long countByRoomId(Long roomId);
}