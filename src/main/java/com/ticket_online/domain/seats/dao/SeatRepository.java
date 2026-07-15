package com.ticket_online.domain.seats.dao;

import com.ticket_online.domain.seats.domain.Seat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for Seat entity */
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /** Find all seats for a specific screen */
    @Query(
            "SELECT s FROM Seat s WHERE s.screen.id = :screenId AND s.isActive = true ORDER BY s.row, s.number")
    List<Seat> findByScreenId(@Param("screenId") Long screenId);

    /** Find all active seats for a screen */
    @Query(
            "SELECT s FROM Seat s WHERE s.screen.id = :screenId AND s.isActive = true ORDER BY s.row, s.number")
    List<Seat> findActiveByScreenId(@Param("screenId") Long screenId);

    /** Find seat by screen, row, and number */
    @Query(
            "SELECT s FROM Seat s WHERE s.screen.id = :screenId AND s.row = :row AND s.number = :number")
    Optional<Seat> findByScreenIdAndRowAndNumber(
            @Param("screenId") Long screenId,
            @Param("row") String row,
            @Param("number") Integer number);

    /** Find all seats of a specific type in a screen */
    @Query(
            "SELECT s FROM Seat s WHERE s.screen.id = :screenId AND s.type = :type AND s.isActive = true")
    List<Seat> findByScreenIdAndType(
            @Param("screenId") Long screenId, @Param("type") SeatType type);

    /** Count total seats in a screen */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.screen.id = :screenId AND s.isActive = true")
    Long countByScreenId(@Param("screenId") Long screenId);

    /** Find seats by IDs */
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.isActive = true")
    List<Seat> findByIdIn(@Param("seatIds") List<Long> seatIds);

    /** Check if seat exists */
    @Query(
            "SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Seat s "
                    + "WHERE s.screen.id = :screenId AND s.row = :row AND s.number = :number")
    boolean existsByScreenIdAndRowAndNumber(
            @Param("screenId") Long screenId,
            @Param("row") String row,
            @Param("number") Integer number);
}
