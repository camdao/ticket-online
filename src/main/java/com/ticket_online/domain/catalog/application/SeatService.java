package com.ticket_online.domain.catalog.application;

import com.ticket_online.domain.catalog.dao.SeatRepository;
import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.Show;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatService {
    private final SeatRepository seatRepository;

    public void createSeatsForShow(Show show, Long totalSeats, Long price) {
        List<Seat> seats = new ArrayList<>();

        for (long i = 1; i <= totalSeats; i++) {
            seats.add(Seat.createSeat(show, "S" + i, BigDecimal.valueOf(price)));
        }

        seatRepository.saveAll(seats);
    }

    @Transactional
    public void markSeatsAsSold(List<Long> seatIds) {

        List<Seat> seats = seatRepository.findAllById(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }

        for (Seat seat : seats) {
            seat.markSold();
        }
    }
}
