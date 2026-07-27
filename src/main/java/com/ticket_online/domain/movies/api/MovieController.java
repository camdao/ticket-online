package com.ticket_online.domain.movies.api;

import com.ticket_online.domain.movies.application.MovieService;
import com.ticket_online.domain.movies.domain.MovieStatus;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
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
                    "Retrieves a list of movies with optional filters for status. Supports sorting by various fields.")
    @GetMapping
    public ResponseEntity<MovieListResponse> getAllMovies(
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Sort sort = Sort.by(direction, sortBy);
        MovieListResponse response = movieService.getAllMovies(status, sort);
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
            summary = "Search movies",
            description =
                    "Searches for movies by keyword in title, description, director, or cast.")
    @GetMapping("/search")
    public ResponseEntity<MovieListResponse> searchMovies(
            @RequestParam(required = true) String keyword) {
        MovieListResponse response = movieService.searchMoviesByKeyword(keyword);
        return ResponseEntity.ok(response);
    }
}
