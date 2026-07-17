package com.ticket_online.domain.rooms;

import com.ticket_online.domain.cinemas.dao.CinemaRepository;
import com.ticket_online.domain.cinemas.dto.request.RoomRequest;
import com.ticket_online.domain.cinemas.dto.response.RoomResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final CinemaRepository cinemaRepository;

    public RoomResponse getRoomById(Long id) {
        Room room = findRoomById(id);
        return RoomResponse.from(room);
    }

    public List<RoomResponse> getRoomsByCinemaId(Long cinemaId) {
        return roomRepository.findByCinemaId(cinemaId).stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    public List<RoomResponse> getRoomsByRoomType(String roomType) {
        return roomRepository.findByRoomType(roomType).stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        validateCinemaExists(request.getCinemaId());
        Room room =
                Room.createRoom(
                        request.getCinemaId(),
                        request.getName(),
                        request.getCapacity(),
                        request.getRoomType());
        Room savedRoom = roomRepository.save(room);
        return RoomResponse.from(savedRoom);
    }

    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = findRoomById(id);
        validateCinemaExists(request.getCinemaId());
        room.updateRoom(request.getName(), request.getCapacity(), request.getRoomType());
        Room updatedRoom = roomRepository.save(room);
        return RoomResponse.from(updatedRoom);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = findRoomById(id);
        roomRepository.delete(room);
    }

    private Room findRoomById(Long id) {
        return roomRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));
    }

    private void validateCinemaExists(Long cinemaId) {
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new CustomException(ErrorCode.CINEMA_NOT_FOUND);
        }
    }
}
