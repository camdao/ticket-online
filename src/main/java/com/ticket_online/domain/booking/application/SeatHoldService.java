package com.ticket_online.domain.booking.application;

import com.ticket_online.domain.booking.dto.request.HoldSeatRequest;
import com.ticket_online.domain.catalog.dao.SeatRepository;
import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.SeatStatus;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.util.RedisSeatScripts;
import com.ticket_online.global.util.UserUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SeatHoldService {

    private final SeatRepository seatRepository;
    private final RedisSeatScripts redisSeatScripts;
    private final UserUtil userUtil;

    public void holdSeats(HoldSeatRequest request) {

        User user = userUtil.getCurrentUser();

        List<Seat> seats =
                seatRepository.findAllByShowIdAndIdIn(request.showId(), request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new CustomException(ErrorCode.SEAT_NOT_FOUND);
        }

        if (seatRepository.existsSoldSeats(request.showId(), request.seatIds(), SeatStatus.SOLD)) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_SOLD);
        }

        redisSeatScripts.holdSeats(request.seatIds(), request.showId(), user.getId(), 360);
    }
}
