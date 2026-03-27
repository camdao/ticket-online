package com.ticket_online.domain.catalog.reponsitory;

import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.SeatStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByShowIdAndIdIn(Long showId, List<Long> seatIds);

    @Query(
            """
    select count(s) > 0
    from Seat s
    where s.show.id = :showId
    and s.id in :seatIds
    and s.status = :status
""")
    boolean existsSoldSeats(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("status") SeatStatus status);

    List<Seat> findByShowId(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :showId AND s.id IN :seatIds")
    List<Seat> findAllByShowIdAndIdInForUpdate(Long showId, List<Long> seatIds);
}
