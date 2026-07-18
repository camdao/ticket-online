package com.ticket_online.domain.rooms;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByCinemaId(Long cinemaId);

    List<Room> findByRoomType(String roomType);

    @Query("SELECT r FROM Room r WHERE r.cinemaId = :cinemaId AND r.roomType = :roomType")
    List<Room> findByCinemaIdAndRoomType(
            @Param("cinemaId") Long cinemaId, @Param("roomType") String roomType);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.cinemaId = :cinemaId")
    Long countByCinemaId(@Param("cinemaId") Long cinemaId);

    @Query("SELECT SUM(r.capacity) FROM Room r WHERE r.cinemaId = :cinemaId")
    Integer getTotalCapacityByCinemaId(@Param("cinemaId") Long cinemaId);

    @Query(
            "SELECT r.cinemaId, COUNT(r) FROM Room r WHERE r.cinemaId IN :cinemaIds GROUP BY"
                    + " r.cinemaId")
    List<Object[]> countByCinemaIds(@Param("cinemaIds") List<Long> cinemaIds);
}
