package com.ticket_online.domain.movies.api;

import com.ticket_online.domain.movies.application.MovieService;
import com.ticket_online.domain.movies.domain.MovieStatus;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieResponse;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<MovieListResponse> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        MovieListResponse response = movieService.getAllMoviesWithFilters(status, genre, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        MovieResponse response = movieService.getMovieById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/showtimes")
    public ResponseEntity<?> getMovieShowtimes(
            @PathVariable Long id,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // Placeholder: Return empty list until Showtimes domain is implemented
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/now-showing")
    public ResponseEntity<MovieListResponse> getNowShowingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        MovieListResponse response = movieService.getNowShowingMovies(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<MovieListResponse> getUpcomingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        MovieListResponse response = movieService.getUpcomingMovies(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<MovieListResponse> searchMovies(
            @RequestParam(required = true) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        MovieListResponse response = movieService.searchMoviesByKeyword(keyword, pageable);
        return ResponseEntity.ok(response);
    }
}
