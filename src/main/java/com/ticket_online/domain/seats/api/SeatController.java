package com.ticket_online.domain.seats.api;

import com.ticket_online.domain.seats.application.SeatService;
import com.ticket_online.domain.seats.domain.SeatType;
import com.ticket_online.domain.seats.dto.request.CreateSeatRequest;
import com.ticket_online.domain.seats.dto.request.UpdateSeatRequest;
import com.ticket_online.domain.seats.dto.response.ScreenLayoutResponse;
import com.ticket_online.domain.seats.dto.response.SeatResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for seat management operations */
@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /** Get all seats for a specific room GET /api/v1/seats?roomId={roomId} */
    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeatsByRoom(@RequestParam Long roomId) {
        List<SeatResponse> seats = seatService.getSeatsByRoomId(roomId);
        return ResponseEntity.ok(seats);
    }

    /** Get room layout information GET /api/v1/seats/layout?roomId={roomId} */
    @GetMapping("/layout")
    public ResponseEntity<ScreenLayoutResponse> getRoomLayout(@RequestParam Long roomId) {
        ScreenLayoutResponse layout = seatService.getRoomLayout(roomId);
        return ResponseEntity.ok(layout);
    }

    /** Create a new seat POST /api/v1/seats */
    @PostMapping
    public ResponseEntity<SeatResponse> createSeat(@Valid @RequestBody CreateSeatRequest request) {
        SeatResponse seat = seatService.createSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    /** Update a seat PUT /api/v1/seats/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<SeatResponse> updateSeat(
            @PathVariable Long id, @Valid @RequestBody UpdateSeatRequest request) {
        SeatResponse seat = seatService.updateSeat(id, request);
        return ResponseEntity.ok(seat);
    }

    /** Delete a seat (soft delete) DELETE /api/v1/seats/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    /** Bulk create seats for a room POST /api/v1/seats/bulk */
    @PostMapping("/bulk")
    public ResponseEntity<List<SeatResponse>> bulkCreateSeats(
            @RequestParam Long roomId,
            @RequestParam String[] rows,
            @RequestParam int seatsPerRow,
            @RequestParam SeatType type,
            @RequestParam Long basePrice) {
        List<SeatResponse> seats =
                seatService.bulkCreateSeats(roomId, rows, seatsPerRow, type, basePrice);
        return ResponseEntity.status(HttpStatus.CREATED).body(seats);
    }
}
