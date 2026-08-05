package com.ticket_online.domain.showtimes.application;

import com.ticket_online.domain.bookings.dao.BookingDetailRepository;
import com.ticket_online.domain.cinemas.dto.response.ShowtimeResponse;
import com.ticket_online.domain.rooms.domain.Room;
import com.ticket_online.domain.seats.dao.SeatRepository;
import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.seats.domain.SeatStatus;
import com.ticket_online.domain.seats.dto.response.SeatResponse;
import com.ticket_online.domain.showtimes.dao.ShowtimeRepository;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeDetailResponse;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeSeatsResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Get detailed information for a specific showtime
     *
     * @param showtimeId the ID of the showtime
     * @return ShowtimeDetailResponse with nested movie and cinema info
     */
    public ShowtimeDetailResponse getShowtimeById(Long showtimeId) {
        // Fetch showtime with movie, cinema, and room details
        Showtime showtime =
                showtimeRepository
                        .findByIdWithDetails(showtimeId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SHOWTIME_NOT_FOUND));

        Room room = showtime.getRoom();

        // Get total seats count for the room
        List<Seat> allSeats = seatRepository.findByRoomId(room.getId());
        int totalSeats = allSeats.size();

        // Get confirmed (BOOKED) seat count from database
        List<Long> confirmedSeatIds =
                bookingDetailRepository.findConfirmedSeatIdsByShowtimeId(showtimeId);
        int bookedSeatsCount = confirmedSeatIds.size();

        // Get held seats count from Redis
        int heldSeatsCount = 0;
        for (Seat seat : allSeats) {
            String redisKey = String.format("seat:hold:%d:%d", showtimeId, seat.getId());
            Boolean isHeld = redisTemplate.hasKey(redisKey);
            if (Boolean.TRUE.equals(isHeld)) {
                heldSeatsCount++;
            }
        }

        // Calculate available seats
        int availableSeats = totalSeats - bookedSeatsCount - heldSeatsCount;

        return ShowtimeDetailResponse.from(showtime, availableSeats, totalSeats);
    }

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

        // Get confirmed (BOOKED) seat IDs from database
        List<Long> confirmedSeatIds =
                bookingDetailRepository.findConfirmedSeatIdsByShowtimeId(showtimeId);
        Set<Long> bookedSeatIds = new HashSet<>(confirmedSeatIds);

        // Batch-check held seats in Redis with a single operation
        List<String> redisKeys =
                seats.stream()
                        .map(seat -> String.format("seat:hold:%d:%d", showtimeId, seat.getId()))
                        .collect(Collectors.toList());

        List<String> redisValues = redisTemplate.opsForValue().multiGet(redisKeys);

        // Build set of held seat IDs (where Redis value is not null)
        Set<Long> heldSeatIds = new HashSet<>();
        if (redisValues != null) {
            for (int i = 0; i < seats.size(); i++) {
                if (redisValues.get(i) != null) {
                    heldSeatIds.add(seats.get(i).getId());
                }
            }
        }

        // Map seats to response with proper status checking
        List<SeatResponse> seatResponses =
                seats.stream()
                        .map(
                                seat -> {
                                    SeatStatus status =
                                            determineSeatStatus(seat, bookedSeatIds, heldSeatIds);
                                    return SeatResponse.from(seat, status);
                                })
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

    /**
     * Determine the status of a seat
     *
     * @param seat the seat to check
     * @param bookedSeatIds set of confirmed (BOOKED) seat IDs
     * @param heldSeatIds set of held seat IDs from Redis
     * @return the seat status (BOOKED, HELD, or AVAILABLE)
     */
    private SeatStatus determineSeatStatus(
            Seat seat, Set<Long> bookedSeatIds, Set<Long> heldSeatIds) {
        Long seatId = seat.getId();

        // Check if seat is confirmed/booked in database
        if (bookedSeatIds.contains(seatId)) {
            return SeatStatus.BOOKED;
        }

        // Check if seat is held in Redis
        if (heldSeatIds.contains(seatId)) {
            return SeatStatus.HELD;
        }

        // Seat is available
        return SeatStatus.AVAILABLE;
    }
}
