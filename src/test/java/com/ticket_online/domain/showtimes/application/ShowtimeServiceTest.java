package com.ticket_online.domain.showtimes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticket_online.domain.cinemas.dao.CinemaRepository;
import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.rooms.Room;
import com.ticket_online.domain.seats.dao.SeatRepository;
import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.seats.domain.SeatStatus;
import com.ticket_online.domain.seats.domain.SeatType;
import com.ticket_online.domain.showtimes.dao.ShowtimeRepository;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeSeatsResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock private ShowtimeRepository showtimeRepository;

    @Mock private SeatRepository seatRepository;
    @Mock private CinemaRepository cinemaRepository;
    @InjectMocks private ShowtimeService showtimeService;

    @Test
    @DisplayName("Should return showtime seats when showtime exists")
    void shouldReturnShowtimeSeatsWhenShowtimeExists() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();
        List<Seat> seats = createTestSeats(showtime.getRoom());

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(showtime.getRoom().getId())).thenReturn(seats);

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getShowtimeId()).isEqualTo(showtimeId);
        assertThat(result.getScreenLayout()).isNotNull();
        assertThat(result.getSeats()).hasSize(6);
        verify(showtimeRepository).findByIdWithDetails(showtimeId);
        verify(seatRepository).findByRoomId(showtime.getRoom().getId());
    }

    @Test
    @DisplayName("Should throw exception when showtime not found")
    void shouldThrowExceptionWhenShowtimeNotFound() {
        // Given
        Long showtimeId = 999L;
        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> showtimeService.getShowtimeSeats(showtimeId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOWTIME_NOT_FOUND);
        verify(showtimeRepository).findByIdWithDetails(showtimeId);
    }

    @Test
    @DisplayName("Should return all seats with AVAILABLE status")
    void shouldReturnAllSeatsWithAvailableStatus() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();
        List<Seat> seats = createTestSeats(showtime.getRoom());

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(showtime.getRoom().getId())).thenReturn(seats);

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result.getSeats())
                .isNotNull()
                .allMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should build screen layout correctly with multiple rows")
    void shouldBuildScreenLayoutCorrectlyWithMultipleRows() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();
        List<Seat> seats = createTestSeats(showtime.getRoom());

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(showtime.getRoom().getId())).thenReturn(seats);

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result.getScreenLayout()).isNotNull();
        assertThat(result.getScreenLayout().getRows()).containsExactly("A", "B", "C");
        assertThat(result.getScreenLayout().getSeatsPerRow()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should handle empty seats list")
    void shouldHandleEmptySeatsList() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(showtime.getRoom().getId()))
                .thenReturn(Collections.emptyList());

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSeats()).isEmpty();
        assertThat(result.getScreenLayout().getRows()).isEmpty();
        assertThat(result.getScreenLayout().getSeatsPerRow()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return seats with correct seat information")
    void shouldReturnSeatsWithCorrectSeatInformation() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();
        List<Seat> seats = createTestSeats(showtime.getRoom());

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(showtime.getRoom().getId())).thenReturn(seats);

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result.getSeats()).hasSize(6);
        assertThat(result.getSeats())
                .extracting("row")
                .containsExactly("A", "A", "A", "B", "B", "C");
        assertThat(result.getSeats()).extracting("number").containsExactly(1, 2, 3, 1, 2, 1);
    }

    @Test
    @DisplayName("Should include different seat types")
    void shouldIncludeDifferentSeatTypes() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();
        List<Seat> seats = createTestSeatsWithDifferentTypes(showtime.getRoom());

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(showtime.getRoom().getId())).thenReturn(seats);

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result.getSeats())
                .extracting("type")
                .containsExactlyInAnyOrder(
                        SeatType.REGULAR, SeatType.REGULAR, SeatType.VIP, SeatType.COUPLE);
    }

    @Test
    @DisplayName("Should include seat prices")
    void shouldIncludeSeatPrices() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();
        List<Seat> seats = createTestSeatsWithDifferentTypes(showtime.getRoom());

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(showtime.getRoom().getId())).thenReturn(seats);

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result.getSeats())
                .allMatch(seat -> seat.getPrice() != null && seat.getPrice() > 0);
    }

    @Test
    @DisplayName("Should handle single row with multiple seats")
    void shouldHandleSingleRowWithMultipleSeats() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();
        Room room = showtime.getRoom();

        List<Seat> seats =
                Arrays.asList(
                        createSeat(room, "A", 1, SeatType.REGULAR, 85000L),
                        createSeat(room, "A", 2, SeatType.REGULAR, 85000L),
                        createSeat(room, "A", 3, SeatType.REGULAR, 85000L),
                        createSeat(room, "A", 4, SeatType.REGULAR, 85000L),
                        createSeat(room, "A", 5, SeatType.REGULAR, 85000L));

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(room.getId())).thenReturn(seats);

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result.getScreenLayout().getRows()).containsExactly("A");
        assertThat(result.getScreenLayout().getSeatsPerRow()).isEqualTo(5);
        assertThat(result.getSeats()).hasSize(5);
    }

    @Test
    @DisplayName("Should calculate max seats per row correctly with uneven rows")
    void shouldCalculateMaxSeatsPerRowCorrectlyWithUnevenRows() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();
        Room room = showtime.getRoom();

        List<Seat> seats =
                Arrays.asList(
                        createSeat(room, "A", 1, SeatType.REGULAR, 85000L),
                        createSeat(room, "A", 2, SeatType.REGULAR, 85000L),
                        createSeat(room, "B", 1, SeatType.REGULAR, 85000L),
                        createSeat(room, "B", 2, SeatType.REGULAR, 85000L),
                        createSeat(room, "B", 3, SeatType.REGULAR, 85000L),
                        createSeat(room, "B", 4, SeatType.REGULAR, 85000L),
                        createSeat(room, "C", 1, SeatType.REGULAR, 85000L));

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(room.getId())).thenReturn(seats);

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result.getScreenLayout().getRows()).containsExactly("A", "B", "C");
        assertThat(result.getScreenLayout().getSeatsPerRow())
                .isEqualTo(4); // Max is row B with 4 seats
        assertThat(result.getSeats()).hasSize(7);
    }

    @Test
    @DisplayName("Should return rows in sorted order")
    void shouldReturnRowsInSortedOrder() {
        // Given
        Long showtimeId = 1L;
        Showtime showtime = createTestShowtime();
        Room room = showtime.getRoom();

        // Create seats in non-alphabetical order
        List<Seat> seats =
                Arrays.asList(
                        createSeat(room, "C", 1, SeatType.REGULAR, 85000L),
                        createSeat(room, "A", 1, SeatType.REGULAR, 85000L),
                        createSeat(room, "B", 1, SeatType.REGULAR, 85000L));

        when(showtimeRepository.findByIdWithDetails(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomId(room.getId())).thenReturn(seats);

        // When
        ShowtimeSeatsResponse result = showtimeService.getShowtimeSeats(showtimeId);

        // Then
        assertThat(result.getScreenLayout().getRows()).containsExactly("A", "B", "C");
    }

    // Helper methods to create test data

    private Showtime createTestShowtime() {
        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom",
                        "CGV",
                        "logo.png",
                        "Address",
                        "District 1",
                        "HCMC",
                        "123456",
                        "website",
                        "desc");
        Cinema cinemaSave = cinemaRepository.save(cinema);
        setId(cinema, 1L);

        Room room = Room.createRoom(cinemaSave.getId(), "Screen 1", 100, "VIP");
        setId(room, 1L);

        Movie movie =
                Movie.createMovie(
                        "Avatar 2",
                        192,
                        "Description",
                        "poster.jpg",
                        "trailer.mp4",
                        LocalDate.now(),
                        "Action",
                        "James Cameron",
                        "Cast",
                        "8.5");
        setId(movie, 1L);

        Showtime showtime =
                Showtime.createShowtime(
                        movie,
                        room,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(3),
                        BigDecimal.valueOf(100000));
        setId(showtime, 1L);

        return showtime;
    }

    private List<Seat> createTestSeats(Room room) {
        return Arrays.asList(
                createSeat(room, "A", 1, SeatType.REGULAR, 85000L),
                createSeat(room, "A", 2, SeatType.REGULAR, 85000L),
                createSeat(room, "A", 3, SeatType.REGULAR, 85000L),
                createSeat(room, "B", 1, SeatType.REGULAR, 85000L),
                createSeat(room, "B", 2, SeatType.REGULAR, 85000L),
                createSeat(room, "C", 1, SeatType.VIP, 120000L));
    }

    private List<Seat> createTestSeatsWithDifferentTypes(Room room) {
        return Arrays.asList(
                createSeat(room, "A", 1, SeatType.REGULAR, 85000L),
                createSeat(room, "A", 2, SeatType.REGULAR, 85000L),
                createSeat(room, "B", 1, SeatType.VIP, 120000L),
                createSeat(room, "C", 1, SeatType.COUPLE, 200000L));
    }

    private Seat createSeat(Room room, String row, Integer number, SeatType type, Long price) {
        Seat seat =
                Seat.builder()
                        .room(room)
                        .row(row)
                        .number(number)
                        .type(type)
                        .basePrice(price)
                        .build();
        // Set a unique ID based on row and number for testing
        setId(seat, Long.valueOf(row.charAt(0) - 'A' + 1) * 100 + number);
        return seat;
    }

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID via reflection", e);
        }
    }
}
