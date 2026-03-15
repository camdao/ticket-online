package com.ticket_online.domain.catalog.api;

import com.ticket_online.domain.catalog.application.ShowService;
import com.ticket_online.domain.catalog.dto.CreateShowRequest;
import com.ticket_online.domain.catalog.dto.CreateShowResponse;
import com.ticket_online.domain.catalog.dto.FindShowResponse;
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
                        request.totalSeats()));
    }
}
