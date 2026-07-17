package com.ticket_online.domain.cinemas.api;

import com.ticket_online.domain.cinemas.application.CinemaService;
import com.ticket_online.domain.cinemas.dto.response.CinemaListResponse;
import com.ticket_online.domain.cinemas.dto.response.CinemaResponse;
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

        CinemaListResponse response;

        if (city != null && district != null) {
            response = cinemaService.getCinemasByCityAndDistrict(city, district);
        } else if (brand != null) {
            response = cinemaService.getCinemasByBrand(brand);
        } else if (city != null) {
            response = cinemaService.getCinemasByCity(city);
        } else {
            Pageable pageable = PageRequest.of(page, size);
            response = cinemaService.getAllCinemas(pageable);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/showtimes")
    public ResponseEntity<?> getCinemaShowtimes(
            @PathVariable Long id,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(
                cinemaService.getCinemaShowtimes(id, movieId, date, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CinemaResponse> getCinemaById(@PathVariable Long id) {
        CinemaResponse response = cinemaService.getCinemaById(id);
        return ResponseEntity.ok(response);
    }
}
