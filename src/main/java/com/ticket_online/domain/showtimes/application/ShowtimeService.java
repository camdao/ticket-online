package com.ticket_online.domain.showtimes.application;

import com.ticket_online.domain.cinemas.dto.response.ShowtimeResponse;
import com.ticket_online.domain.rooms.Room;
import com.ticket_online.domain.seats.dao.SeatRepository;
import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.seats.domain.SeatStatus;
import com.ticket_online.domain.seats.dto.response.SeatResponse;
import com.ticket_online.domain.showtimes.dao.ShowtimeRepository;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.domain.ShowtimeStatus;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeSeatsResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;

    /**
     * Get seat availability for a specific showtime
     *
     * @param showtimeId the ID of the showtime
     * @return ShowtimeSeatsResponse containing showtime ID and seat list
     */
    public ShowtimeSeatsResponse getShowtimeSeats(Long showtimeId) {
        // Fetch showtime with room details
        Showtime showtime =
                showtimeRepository
                        .findByIdWithDetails(showtimeId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SHOWTIME_NOT_FOUND));

        Room room = showtime.getRoom();

        // Fetch all active seats for the room
        List<Seat> seats = seatRepository.findByRoomId(room.getId());

        // Map seats to response with AVAILABLE status (Option B - simplified version)
        // TODO: Implement Redis check for HELD status and database check for BOOKED status
        List<SeatResponse> seatResponses =
                seats.stream()
                        .map(seat -> SeatResponse.from(seat, SeatStatus.AVAILABLE))
                        .collect(Collectors.toList());

        return ShowtimeSeatsResponse.of(showtimeId, seatResponses);
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
