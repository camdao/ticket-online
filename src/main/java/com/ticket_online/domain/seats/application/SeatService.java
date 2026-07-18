package com.ticket_online.domain.seats.application;

import com.ticket_online.domain.rooms.Room;
import com.ticket_online.domain.rooms.RoomRepository;
import com.ticket_online.domain.seats.dao.SeatRepository;
import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.seats.domain.SeatType;
import com.ticket_online.domain.seats.dto.request.CreateSeatRequest;
import com.ticket_online.domain.seats.dto.request.UpdateSeatRequest;
import com.ticket_online.domain.seats.dto.response.ScreenLayoutResponse;
import com.ticket_online.domain.seats.dto.response.SeatResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing seats in cinema rooms */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;

    /** Get all seats for a room */
    public List<SeatResponse> getSeatsByRoomId(Long roomId) {
        validateRoomExists(roomId);

        List<Seat> seats = seatRepository.findActiveByRoomId(roomId);
        return seats.stream().map(SeatResponse::from).collect(Collectors.toList());
    }

    /** Get room layout information */
    public ScreenLayoutResponse getRoomLayout(Long roomId) {
        validateRoomExists(roomId);

        List<Seat> seats = seatRepository.findActiveByRoomId(roomId);

        if (seats.isEmpty()) {
            return ScreenLayoutResponse.of(List.of(), 0);
        }

        // Get unique rows and sort them
        List<String> rows =
                seats.stream().map(Seat::getRow).distinct().sorted().collect(Collectors.toList());

        // Get max seats per row
        Integer maxSeatsPerRow =
                seats.stream().map(Seat::getNumber).max(Comparator.naturalOrder()).orElse(0);

        return ScreenLayoutResponse.of(rows, maxSeatsPerRow);
    }

    /** Create a new seat */
    @Transactional
    public SeatResponse createSeat(CreateSeatRequest request) {
        // Validate room exists
        Room room =
                roomRepository
                        .findById(request.getRoomId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        // Check if seat already exists
        if (seatRepository.existsByRoomIdAndRowAndNumber(
                request.getRoomId(), request.getRow(), request.getNumber())) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_EXISTS);
        }

        // Create seat
        Seat seat =
                Seat.builder()
                        .room(room)
                        .row(request.getRow())
                        .number(request.getNumber())
                        .type(request.getType())
                        .basePrice(request.getBasePrice())
                        .build();

        Seat savedSeat = seatRepository.save(seat);
        log.info("Created new seat: {} for room: {}", savedSeat.getSeatLabel(), room.getName());

        return SeatResponse.from(savedSeat);
    }

    /** Update a seat */
    @Transactional
    public SeatResponse updateSeat(Long seatId, UpdateSeatRequest request) {
        Seat seat =
                seatRepository
                        .findById(seatId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        if (request.getType() != null) {
            seat.updateType(request.getType());
        }

        if (request.getBasePrice() != null) {
            seat.updateBasePrice(request.getBasePrice());
        }

        if (request.getIsActive() != null) {
            if (request.getIsActive()) {
                seat.activate();
            } else {
                seat.deactivate();
            }
        }

        Seat updatedSeat = seatRepository.save(seat);
        log.info("Updated seat: {}", updatedSeat.getSeatLabel());

        return SeatResponse.from(updatedSeat);
    }

    /** Delete a seat (soft delete by deactivating) */
    @Transactional
    public void deleteSeat(Long seatId) {
        Seat seat =
                seatRepository
                        .findById(seatId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        seat.deactivate();
        seatRepository.save(seat);

        log.info("Deactivated seat: {}", seat.getSeatLabel());
    }

    /** Bulk create seats for a room (useful for initial setup) */
    @Transactional
    public List<SeatResponse> bulkCreateSeats(
            Long roomId, String[] rows, int seatsPerRow, SeatType defaultType, Long basePrice) {
        Room room =
                roomRepository
                        .findById(roomId)
                        .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        List<Seat> seats = new java.util.ArrayList<>();

        for (String row : rows) {
            for (int number = 1; number <= seatsPerRow; number++) {
                // Check if seat already exists
                if (!seatRepository.existsByRoomIdAndRowAndNumber(roomId, row, number)) {
                    Seat seat =
                            Seat.builder()
                                    .room(room)
                                    .row(row)
                                    .number(number)
                                    .type(defaultType)
                                    .basePrice(basePrice)
                                    .build();
                    seats.add(seat);
                }
            }
        }

        List<Seat> savedSeats = seatRepository.saveAll(seats);
        log.info("Bulk created {} seats for room: {}", savedSeats.size(), room.getName());

        return savedSeats.stream().map(SeatResponse::from).collect(Collectors.toList());
    }

    /** Get seats by IDs */
    public List<Seat> getSeatsByIds(List<Long> seatIds) {
        return seatRepository.findByIdIn(seatIds);
    }

    private void validateRoomExists(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
        }
    }
}
