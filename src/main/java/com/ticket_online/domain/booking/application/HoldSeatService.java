package com.ticket_online.domain.booking.application;

import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.SeatStatus;
import com.ticket_online.domain.catalog.reponsitory.SeatRepository;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.util.RedisSeatScripts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Service
public class HoldSeatService {

    private final SeatRepository seatRepository;
    private final RedisSeatScripts redisSeatScripts;

    public void holdSeats(Long showId, List<Long> seatIds, Long userId) {

        List<Seat> seats = seatRepository.findAllByShowIdAndIdIn(showId, seatIds);
        if (seats.size() != seatIds.size()) {
            throw new CustomException(ErrorCode.SEAT_NOT_FOUND);
        }

        if (seatRepository.existsSoldSeats(showId, seatIds, SeatStatus.SOLD)) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_SOLD);
        }

        redisSeatScripts.holdSeats(seatIds, showId, userId, 360);

    }

}
