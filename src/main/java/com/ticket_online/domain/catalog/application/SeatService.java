package com.ticket_online.domain.catalog.application;

import com.ticket_online.domain.catalog.dao.SeatRepository;
import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.Show;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatService {
    private final SeatRepository seatRepository;

    //    N+1
    public void createSeatsForShow(Show show, Long totalSeats, Long price) {
        for (long i = 1; i <= totalSeats; i++) {
            seatRepository.save(Seat.createSeat(show, "S" + i, BigDecimal.valueOf(price)));
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
