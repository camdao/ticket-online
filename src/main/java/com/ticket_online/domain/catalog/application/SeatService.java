package com.ticket_online.domain.catalog.application;

import com.ticket_online.domain.catalog.dao.SeatRepository;
import com.ticket_online.domain.catalog.dao.ShowRepository;
import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.Show;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
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
    private final ShowRepository showRepository;

    public void createSeatsForShow(Long showId, int totalSeats, Long price) {
        Show show =
                showRepository
                        .findById(showId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SHOW_NOT_FOUND));

        List<Seat> seats = new ArrayList<>();

        for (int i = 1; i <= totalSeats; i++) {
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
