package com.ticket_online.domain.catalog.reponsitory;

import com.ticket_online.domain.catalog.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    Optional<Seat> findByShowIdAndId(Long showId, Long seatId);
}
