package com.ticket_online.domain.catalog.application;

import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.Show;
import com.ticket_online.domain.catalog.reponsitory.SeatRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatService {
    private final SeatRepository seatRepository;

    //    N+1
    public void createSeatsForShow(Show show, Long totalSeats) {
        for (long i = 1; i <= totalSeats; i++) {
            seatRepository.save(Seat.createSeat(show, "S" + i));
        }
    }

    @Transactional
    public void markSeatsAsSold(Long showId, List<Long> seatIds) {

        List<Seat> seats = seatRepository.findAllByShowIdAndIdInForUpdate(showId, seatIds);

        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }

        for (Seat seat : seats) {
            seat.markSold();
        }
    }
}
