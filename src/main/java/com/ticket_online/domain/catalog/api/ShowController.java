package com.ticket_online.domain.catalog.api;

import com.ticket_online.domain.catalog.application.ShowService;
import com.ticket_online.domain.catalog.dto.request.CreateShowRequest;
import com.ticket_online.domain.catalog.dto.response.CreateShowResponse;
import com.ticket_online.domain.catalog.dto.response.FindShowResponse;
import com.ticket_online.domain.catalog.dto.response.SeatResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shows")
@RequiredArgsConstructor
public class ShowController {
    private final ShowService showService;

    @GetMapping
    public ResponseEntity<List<FindShowResponse>> findAllShow() {
        return ResponseEntity.ok(showService.findAllShow());
    }

    @PostMapping
    public ResponseEntity<CreateShowResponse> createShow(@RequestBody CreateShowRequest request) {

        return ResponseEntity.ok(
                showService.createShow(
                        request.name(),
                        request.location(),
                        request.startTime(),
                        request.totalSeats(),
                        request.price()));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatResponse>> findSeatsByShow(@PathVariable Long id) {
        return ResponseEntity.ok(showService.findSeatsByShow(id));
    }
}
