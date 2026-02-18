package com.ticket_online.domain.catalog.reponsitory;

import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByShowIdAndIdIn(Long showId, List<Long> seatIds);

    @Query("""
    select count(s) > 0
    from Seat s
    where s.showId = :showId
      and s.id in :seatIds
      and s.status = :status
""")
    boolean existsSoldSeats(@Param("showId") Long showId,
                            @Param("seatIds") List<Long> seatIds,
                            @Param("status") SeatStatus status);
}

