package com.ticket_online.domain.movies.api;

import com.ticket_online.domain.movies.application.MovieService;
import com.ticket_online.domain.movies.domain.MovieStatus;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
            summary = "Get all movies",
            description =
                    "Retrieves a paginated list of movies with optional filters for status and genre. Supports sorting by various fields.")
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

    @Operation(
            summary = "Get movie by ID",
            description = "Retrieves detailed information about a specific movie by its ID.")
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        MovieResponse response = movieService.getMovieById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get movie showtimes",
            description =
                    "Retrieves all showtimes for a specific movie with optional filters for cinema, city, and date range.")
    @GetMapping("/{id}/showtimes")
    public ResponseEntity<?> getMovieShowtimes(
            @PathVariable Long id,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(
                movieService.getMovieShowtimes(id, cinemaId, city, date, startDate, endDate));
    }

    @Operation(
            summary = "Get now showing movies",
            description = "Retrieves a paginated list of movies currently showing in cinemas.")
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

    @Operation(
            summary = "Get upcoming movies",
            description = "Retrieves a paginated list of movies scheduled for future release.")
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

    @Operation(
            summary = "Search movies",
            description =
                    "Searches for movies by keyword in title, description, director, or cast. Returns a paginated list of matching results.")
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
