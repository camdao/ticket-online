package com.ticket_online.domain.movies.api;

import com.ticket_online.domain.movies.application.MovieService;
import com.ticket_online.domain.movies.dto.MovieListResponse;
import com.ticket_online.domain.movies.dto.MovieRequest;
import com.ticket_online.domain.movies.dto.MovieResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id, @Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.updateMovie(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        MovieResponse response = movieService.getMovieById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<MovieListResponse> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        MovieListResponse response = movieService.getAllMovies(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<MovieListResponse> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String director,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        MovieListResponse response =
                movieService.searchMovies(title, genre, rating, director, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/title")
    public ResponseEntity<MovieListResponse> searchMoviesByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        MovieListResponse response = movieService.searchMoviesByTitle(title, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/genre")
    public ResponseEntity<MovieListResponse> searchMoviesByGenre(
            @RequestParam String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        MovieListResponse response = movieService.searchMoviesByGenre(genre, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/rating")
    public ResponseEntity<MovieListResponse> searchMoviesByRating(
            @RequestParam String rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        MovieListResponse response = movieService.searchMoviesByRating(rating, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/director")
    public ResponseEntity<MovieListResponse> searchMoviesByDirector(
            @RequestParam String director,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        MovieListResponse response = movieService.searchMoviesByDirector(director, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<MovieListResponse> getUpcomingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "releaseDate"));
        MovieListResponse response = movieService.getUpcomingMovies(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/now-showing")
    public ResponseEntity<MovieListResponse> getNowShowingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "releaseDate"));
        MovieListResponse response = movieService.getNowShowingMovies(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-release-date")
    public ResponseEntity<MovieListResponse> getMoviesByReleaseDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "releaseDate"));
        MovieListResponse response =
                movieService.getMoviesByReleaseDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<MovieResponse>> getMoviesByIds(@RequestParam List<Long> ids) {
        List<MovieResponse> response = movieService.getMoviesByIds(ids);
        return ResponseEntity.ok(response);
    }
}
