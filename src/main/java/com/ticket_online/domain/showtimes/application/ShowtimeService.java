package com.ticket_online.domain.showtimes.application;

import com.ticket_online.domain.showtimes.dao.ShowtimeRepository;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.domain.ShowtimeStatus;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeDetailResponse;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeListResponse;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    public ShowtimeDetailResponse getShowtimeById(Long id) {
        Showtime showtime =
                showtimeRepository
                        .findByIdWithDetails(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.SHOWTIME_NOT_FOUND));
        return ShowtimeDetailResponse.from(showtime);
    }

    public ShowtimeListResponse searchShowtimes(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate,
            Pageable pageable) {

        Specification<Showtime> spec = Specification.where(null);

        // Filter by movie
        if (movieId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("movie").get("id"), movieId));
        }

        // Filter by cinema
        if (cinemaId != null) {
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.equal(root.get("room").get("cinema").get("id"), cinemaId));
        }

        // Filter by city
        if (city != null && !city.isBlank()) {
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.equal(root.get("room").get("cinema").get("city"), city));
        }

        // Filter by date
        if (date != null && !date.isBlank()) {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.between(root.get("startTime"), startOfDay, endOfDay));
        } else if (startDate != null && !startDate.isBlank()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.greaterThanOrEqualTo(root.get("startTime"), start));

            if (endDate != null && !endDate.isBlank()) {
                LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                spec =
                        spec.and(
                                (root, query, cb) ->
                                        cb.lessThanOrEqualTo(root.get("startTime"), end));
            }
        }

        // Only active showtimes
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), ShowtimeStatus.ACTIVE));

        // Future showtimes only
        spec =
                spec.and(
                        (root, query, cb) ->
                                cb.greaterThanOrEqualTo(
                                        root.get("startTime"), LocalDateTime.now()));

        // Fetch with details
        spec =
                spec.and(
                        (root, query, cb) -> {
                            root.fetch("movie");
                            root.fetch("room").fetch("cinema");
                            return cb.conjunction();
                        });

        Page<Showtime> showtimePage = showtimeRepository.findAll(spec, pageable);

        List<ShowtimeResponse> content =
                showtimePage.getContent().stream().map(ShowtimeResponse::from).toList();

        return ShowtimeListResponse.of(
                content,
                showtimePage.getNumber(),
                showtimePage.getSize(),
                showtimePage.getTotalElements(),
                showtimePage.getTotalPages(),
                showtimePage.isFirst(),
                showtimePage.isLast());
    }

    public List<ShowtimeResponse> getShowtimesByMovieId(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate) {

        Specification<Showtime> spec = Specification.where(null);

        // Filter by movie (required)
        spec = spec.and((root, query, cb) -> cb.equal(root.get("movie").get("id"), movieId));

        // Filter by cinema
        if (cinemaId != null) {
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.equal(root.get("room").get("cinema").get("id"), cinemaId));
        }

        // Filter by city
        if (city != null && !city.isBlank()) {
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.equal(root.get("room").get("cinema").get("city"), city));
        }

        // Filter by date
        if (date != null && !date.isBlank()) {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.between(root.get("startTime"), startOfDay, endOfDay));
        } else if (startDate != null && !startDate.isBlank()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.greaterThanOrEqualTo(root.get("startTime"), start));

            if (endDate != null && !endDate.isBlank()) {
                LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                spec =
                        spec.and(
                                (root, query, cb) ->
                                        cb.lessThanOrEqualTo(root.get("startTime"), end));
            }
        }

        // Only active showtimes
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), ShowtimeStatus.ACTIVE));

        // Future showtimes only
        spec =
                spec.and(
                        (root, query, cb) ->
                                cb.greaterThanOrEqualTo(
                                        root.get("startTime"), LocalDateTime.now()));

        // Fetch with details
        spec =
                spec.and(
                        (root, query, cb) -> {
                            root.fetch("movie");
                            root.fetch("room").fetch("cinema");
                            return cb.conjunction();
                        });

        List<Showtime> showtimes = showtimeRepository.findAll(spec);
        return showtimes.stream().map(ShowtimeResponse::from).toList();
    }

    public List<ShowtimeResponse> getShowtimesByCinemaId(
            Long cinemaId, Long movieId, String date, String startDate, String endDate) {

        Specification<Showtime> spec = Specification.where(null);

        // Filter by cinema (required)
        spec =
                spec.and(
                        (root, query, cb) ->
                                cb.equal(root.get("room").get("cinema").get("id"), cinemaId));

        // Filter by movie
        if (movieId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("movie").get("id"), movieId));
        }

        // Filter by date
        if (date != null && !date.isBlank()) {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.between(root.get("startTime"), startOfDay, endOfDay));
        } else if (startDate != null && !startDate.isBlank()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            spec =
                    spec.and(
                            (root, query, cb) ->
                                    cb.greaterThanOrEqualTo(root.get("startTime"), start));

            if (endDate != null && !endDate.isBlank()) {
                LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                spec =
                        spec.and(
                                (root, query, cb) ->
                                        cb.lessThanOrEqualTo(root.get("startTime"), end));
            }
        }

        // Only active showtimes
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), ShowtimeStatus.ACTIVE));

        // Future showtimes only
        spec =
                spec.and(
                        (root, query, cb) ->
                                cb.greaterThanOrEqualTo(
                                        root.get("startTime"), LocalDateTime.now()));

        // Fetch with details
        spec =
                spec.and(
                        (root, query, cb) -> {
                            root.fetch("movie");
                            root.fetch("room").fetch("cinema");
                            return cb.conjunction();
                        });

        List<Showtime> showtimes = showtimeRepository.findAll(spec);
        return showtimes.stream().map(ShowtimeResponse::from).toList();
    }
}
