package com.ticket_online.domain.cinemas.api;

import com.ticket_online.domain.cinemas.application.CinemaService;
import com.ticket_online.domain.cinemas.dto.response.CinemaListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    public ResponseEntity<CinemaListResponse> getAllCinemas(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {

        CinemaListResponse response = cinemaService.getCinemas(brand, city, district);

        return ResponseEntity.ok(response);
    }
}
