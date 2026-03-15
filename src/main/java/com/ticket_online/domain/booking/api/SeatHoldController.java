package com.ticket_online.domain.booking.api;

import com.ticket_online.domain.booking.application.HoldSeatService;
import com.ticket_online.domain.booking.dto.request.HoldSeatRequest;
import com.ticket_online.domain.booking.dto.response.HoldSeatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seat-holds")
@RequiredArgsConstructor
public class SeatHoldController {
    private final HoldSeatService holdSeatService;

    @PostMapping
    public ResponseEntity<HoldSeatResponse> holdSeat(@RequestBody @Valid HoldSeatRequest req) {
        holdSeatService.holdSeats(req.showId(), req.seatIds(), req.userId());
        return ResponseEntity.ok(HoldSeatResponse.success(req.showId(), req.seatIds().size()));
    }
}
