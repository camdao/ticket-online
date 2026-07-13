package com.ticket_online.domain.cinemas.api;

import com.ticket_online.domain.cinemas.application.CinemaService;
import com.ticket_online.domain.cinemas.dto.request.CinemaRequest;
import com.ticket_online.domain.cinemas.dto.response.CinemaDetailResponse;
import com.ticket_online.domain.cinemas.dto.response.CinemaListResponse;
import com.ticket_online.domain.cinemas.dto.response.CinemaResponse;
import com.ticket_online.domain.cinemas.dto.response.ScreenResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    public ResponseEntity<CinemaListResponse> getAllCinemas(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String keyword) {

        CinemaListResponse response;

        if (keyword != null && !keyword.trim().isEmpty()) {
            response = cinemaService.searchCinemas(keyword);
        } else if (city != null && district != null) {
            response = cinemaService.getCinemasByCityAndDistrict(city, district);
        } else if (brand != null) {
            response = cinemaService.getCinemasByBrand(brand);
        } else if (city != null) {
            response = cinemaService.getCinemasByCity(city);
        } else {
            response = cinemaService.getAllCinemas();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CinemaResponse> getCinemaById(@PathVariable Long id) {
        CinemaResponse response = cinemaService.getCinemaById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<CinemaDetailResponse> getCinemaDetail(@PathVariable Long id) {
        CinemaDetailResponse response = cinemaService.getCinemaDetail(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/screens")
    public ResponseEntity<List<ScreenResponse>> getScreensByCinemaId(@PathVariable Long id) {
        List<ScreenResponse> response = cinemaService.getScreensByCinemaId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        List<String> brands = cinemaService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getAllCities() {
        List<String> cities = cinemaService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @PostMapping
    public ResponseEntity<CinemaResponse> createCinema(@Valid @RequestBody CinemaRequest request) {
        CinemaResponse response = cinemaService.createCinema(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CinemaResponse> updateCinema(
            @PathVariable Long id, @Valid @RequestBody CinemaRequest request) {
        CinemaResponse response = cinemaService.updateCinema(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCinema(@PathVariable Long id) {
        cinemaService.deleteCinema(id);
        return ResponseEntity.noContent().build();
    }
}
