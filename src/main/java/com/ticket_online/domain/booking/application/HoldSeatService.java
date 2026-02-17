package com.ticket_online.domain.booking.application;

import com.ticket_online.domain.booking.repository.RedisSeatHoldRepository;
import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.reponsitory.SeatRepository;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Service
public class HoldSeatService {

    private static final Duration HOLD_TTL = Duration.ofMinutes(5);

    private final SeatRepository seatRepository;
    private final RedisSeatHoldRepository seatHoldRepository;

    public void holdSeats(Long showId, List<Long> seatIds, Long userId) {

        if (seatIds.size() > 4) {
            throw new CustomException(ErrorCode.TOO_MANY_SEATS);
        }

        for (Long seatId : seatIds) {

            Seat seat =
                    seatRepository
                            .findByShowIdAndId(showId, seatId)
                            .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NotFound));

            if (seat.isSold()) {
                throw new CustomException(ErrorCode.SEAT_ALREADY_SOLD);
            }

            boolean success = seatHoldRepository.hold(showId, seatId, userId, HOLD_TTL);

            if (!success) {
                throw new CustomException(ErrorCode.SEAT_ALREADY_HELD);
            }
        }
    }
}
