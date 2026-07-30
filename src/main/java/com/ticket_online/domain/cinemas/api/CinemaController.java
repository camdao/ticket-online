package com.ticket_online.domain.cinemas.api;

import com.ticket_online.domain.cinemas.application.CinemaService;
import com.ticket_online.domain.cinemas.dto.response.CinemaListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    public ResponseEntity<CinemaListResponse> getAllCinemas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {

        Pageable pageable = PageRequest.of(page, size);
        CinemaListResponse response = cinemaService.getCinemas(pageable, brand, city, district);

        return ResponseEntity.ok(response);
    }
}
