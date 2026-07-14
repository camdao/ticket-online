package com.ticket_online.domain.cinemas.api;

import com.ticket_online.domain.cinemas.application.ScreenService;
import com.ticket_online.domain.cinemas.dto.request.ScreenRequest;
import com.ticket_online.domain.cinemas.dto.response.ScreenResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    @GetMapping("/{id}")
    public ResponseEntity<ScreenResponse> getScreenById(@PathVariable Long id) {
        ScreenResponse response = screenService.getScreenById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ScreenResponse>> getScreens(
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) String roomType) {

        List<ScreenResponse> response;

        if (cinemaId != null) {
            response = screenService.getScreensByCinemaId(cinemaId);
        } else if (roomType != null) {
            response = screenService.getScreensByRoomType(roomType);
        } else {
            response = List.of();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ScreenResponse> createScreen(@Valid @RequestBody ScreenRequest request) {
        ScreenResponse response = screenService.createScreen(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScreenResponse> updateScreen(
            @PathVariable Long id, @Valid @RequestBody ScreenRequest request) {
        ScreenResponse response = screenService.updateScreen(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }
}
