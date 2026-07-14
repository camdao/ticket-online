package com.ticket_online.domain.cinemas.application;

import com.ticket_online.domain.cinemas.dao.CinemaRepository;
import com.ticket_online.domain.cinemas.dao.ScreenRepository;
import com.ticket_online.domain.cinemas.domain.Screen;
import com.ticket_online.domain.cinemas.dto.request.ScreenRequest;
import com.ticket_online.domain.cinemas.dto.response.ScreenResponse;
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
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final CinemaRepository cinemaRepository;

    public ScreenResponse getScreenById(Long id) {
        Screen screen = findScreenById(id);
        return ScreenResponse.from(screen);
    }

    public List<ScreenResponse> getScreensByCinemaId(Long cinemaId) {
        return screenRepository.findByCinemaId(cinemaId).stream()
                .map(ScreenResponse::from)
                .collect(Collectors.toList());
    }

    public List<ScreenResponse> getScreensByRoomType(String roomType) {
        return screenRepository.findByRoomType(roomType).stream()
                .map(ScreenResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScreenResponse createScreen(ScreenRequest request) {
        validateCinemaExists(request.getCinemaId());
        Screen screen =
                Screen.createScreen(
                        request.getCinemaId(),
                        request.getName(),
                        request.getCapacity(),
                        request.getRoomType());
        Screen savedScreen = screenRepository.save(screen);
        return ScreenResponse.from(savedScreen);
    }

    @Transactional
    public ScreenResponse updateScreen(Long id, ScreenRequest request) {
        Screen screen = findScreenById(id);
        validateCinemaExists(request.getCinemaId());
        screen.updateScreen(request.getName(), request.getCapacity(), request.getRoomType());
        Screen updatedScreen = screenRepository.save(screen);
        return ScreenResponse.from(updatedScreen);
    }

    @Transactional
    public void deleteScreen(Long id) {
        Screen screen = findScreenById(id);
        screenRepository.delete(screen);
    }

    private Screen findScreenById(Long id) {
        return screenRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCREEN_NOT_FOUND));
    }

    private void validateCinemaExists(Long cinemaId) {
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new CustomException(ErrorCode.CINEMA_NOT_FOUND);
        }
    }
}
