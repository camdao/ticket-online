package com.ticket_online.domain.showtimes.application;

import com.ticket_online.domain.cinemas.dto.response.ShowtimeResponse;
import com.ticket_online.domain.rooms.Room;
import com.ticket_online.domain.seats.dao.SeatRepository;
import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.seats.domain.SeatStatus;
import com.ticket_online.domain.seats.dto.response.SeatResponse;
import com.ticket_online.domain.showtimes.dao.ShowtimeRepository;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeSeatsResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Get showtimes with optional filters
     *
     * @param movieId optional movie ID filter
     * @param cinemaId optional cinema ID filter
     * @param city optional city filter
     * @param date optional specific date filter (YYYY-MM-DD)
     * @param startDate optional start date range filter (YYYY-MM-DD)
     * @param endDate optional end date range filter (YYYY-MM-DD)
     * @return list of showtimes matching the filters
     */
    public List<ShowtimeResponse> getShowtimes(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate) {

        List<Showtime> showtimes =
                showtimeRepository.findShowtimesWithFilters(
                        movieId, cinemaId, city, date, startDate, endDate);

        return showtimes.stream().map(ShowtimeResponse::from).toList();
    }

    public List<ShowtimeResponse> getShowtimesByMovieId(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate) {

        List<Showtime> showtimes =
                showtimeRepository.findShowtimesByMovieId(
                        movieId, cinemaId, city, date, startDate, endDate);

        return showtimes.stream().map(ShowtimeResponse::from).toList();
    }

    public List<ShowtimeResponse> getShowtimesByCinemaId(
            Long cinemaId, Long movieId, String date, String startDate, String endDate) {

        List<Showtime> showtimes =
                showtimeRepository.findShowtimesByCinemaId(
                        cinemaId, movieId, date, startDate, endDate);

        return showtimes.stream().map(ShowtimeResponse::from).toList();
    }
}
