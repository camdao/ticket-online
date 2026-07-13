package com.ticket_online.domain.cinemas.dao;

import com.ticket_online.domain.cinemas.domain.Screen;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByCinemaId(Long cinemaId);

    List<Screen> findByRoomType(String roomType);

    @Query("SELECT s FROM Screen s WHERE s.cinemaId = :cinemaId AND s.roomType = :roomType")
    List<Screen> findByCinemaIdAndRoomType(
            @Param("cinemaId") Long cinemaId, @Param("roomType") String roomType);

    @Query("SELECT COUNT(s) FROM Screen s WHERE s.cinemaId = :cinemaId")
    Long countByCinemaId(@Param("cinemaId") Long cinemaId);

    @Query("SELECT SUM(s.capacity) FROM Screen s WHERE s.cinemaId = :cinemaId")
    Integer getTotalCapacityByCinemaId(@Param("cinemaId") Long cinemaId);
}
