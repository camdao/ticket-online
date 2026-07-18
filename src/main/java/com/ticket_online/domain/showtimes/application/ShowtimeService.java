package com.ticket_online.domain.showtimes.application;

import com.ticket_online.domain.rooms.Room;
import com.ticket_online.domain.seats.dao.SeatRepository;
import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.seats.domain.SeatStatus;
import com.ticket_online.domain.seats.dto.response.ScreenLayoutResponse;
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
     * Get seat layout and availability for a specific showtime
     *
     * @param showtimeId the ID of the showtime
     * @return ShowtimeSeatsResponse containing showtime ID, screen layout, and seat list
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

        // Build screen layout from seats
        ScreenLayoutResponse screenLayout = buildScreenLayout(seats);

        // Map seats to response with AVAILABLE status (Option B - simplified version)
        // TODO: Implement Redis check for HELD status and database check for BOOKED status
        List<SeatResponse> seatResponses =
                seats.stream()
                        .map(seat -> SeatResponse.from(seat, SeatStatus.AVAILABLE))
                        .collect(Collectors.toList());

        return ShowtimeSeatsResponse.of(showtimeId, screenLayout, seatResponses);
    }

    /**
     * Build screen layout from list of seats
     *
     * @param seats list of seats
     * @return ScreenLayoutResponse containing rows and seats per row
     */
    private ScreenLayoutResponse buildScreenLayout(List<Seat> seats) {
        if (seats.isEmpty()) {
            return ScreenLayoutResponse.of(List.of(), 0);
        }

        // Get distinct rows in sorted order
        List<String> rows =
                seats.stream().map(Seat::getRow).distinct().sorted().collect(Collectors.toList());

        // Calculate maximum number of seats per row
        Integer maxSeatsPerRow =
                seats.stream()
                        .collect(Collectors.groupingBy(Seat::getRow, Collectors.counting()))
                        .values()
                        .stream()
                        .mapToInt(Long::intValue)
                        .max()
                        .orElse(0);

        return ScreenLayoutResponse.of(rows, maxSeatsPerRow);
    }
}
