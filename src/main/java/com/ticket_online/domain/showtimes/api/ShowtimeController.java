package com.ticket_online.domain.showtimes.api;

import com.ticket_online.domain.showtimes.application.ShowtimeService;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeDetailResponse;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping
    public ResponseEntity<ShowtimeListResponse> searchShowtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startTime"));
        ShowtimeListResponse response =
                showtimeService.searchShowtimes(
                        movieId, cinemaId, city, date, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeDetailResponse> getShowtimeById(@PathVariable Long id) {
        ShowtimeDetailResponse response = showtimeService.getShowtimeById(id);
        return ResponseEntity.ok(response);
    }
}
