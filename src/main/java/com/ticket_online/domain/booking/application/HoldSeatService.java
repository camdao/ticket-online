package com.ticket_online.domain.booking.application;

import com.ticket_online.domain.booking.repository.RedisSeatHoldRepository;
import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.SeatStatus;
import com.ticket_online.domain.catalog.reponsitory.SeatRepository;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class HoldSeatService {

    private static final Duration HOLD_TTL = Duration.ofMinutes(5);

    private final SeatRepository seatRepository;
    private final RedisSeatHoldRepository seatHoldRepository;

    public void holdSeats(Long showId, List<Long> seatIds, Long userId) {

        List<Seat> seats = seatRepository.findAllByShowIdAndIdIn(showId, seatIds);
        if (seats.size() != seatIds.size()) {
            throw new CustomException(ErrorCode.SEAT_NOT_FOUND);
        }

        if (seatRepository.existsSoldSeats(showId, seatIds, SeatStatus.SOLD)) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_SOLD);
        }

        List<Long> heldSeats = new ArrayList<>();

        try {
            for (Long seatId : seatIds) {
                if (!seatHoldRepository.hold(showId, seatId, userId, HOLD_TTL)) {
                    throw new CustomException(ErrorCode.SEAT_ALREADY_HELD);
                }
                heldSeats.add(seatId);
            }
        } catch (RuntimeException ex) {
            rollbackHold(showId, heldSeats);
            throw ex;
        }
    }

    private void rollbackHold(Long showId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            seatHoldRepository.release(showId, seatId);
        }
    }
}
