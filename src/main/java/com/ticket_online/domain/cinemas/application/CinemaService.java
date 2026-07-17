package com.ticket_online.domain.cinemas.application;

import com.ticket_online.domain.cinemas.dao.CinemaRepository;
import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.cinemas.dto.response.CinemaListResponse;
import com.ticket_online.domain.cinemas.dto.response.CinemaResponse;
import com.ticket_online.domain.cinemas.dto.response.RoomResponse;
import com.ticket_online.domain.rooms.RoomRepository;
import com.ticket_online.domain.showtimes.application.ShowtimeService;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeService showtimeService;

    public CinemaListResponse getAllCinemas(Pageable pageable) {
        Page<Cinema> cinemaPage = cinemaRepository.findAll(pageable);
        List<CinemaResponse> cinemaResponses =
                cinemaPage.getContent().stream()
                        .map(
                                cinema -> {
                                    Long totalRoomsLong =
                                            roomRepository.countByCinemaId(cinema.getId());
                                    Integer totalRooms =
                                            totalRoomsLong != null ? totalRoomsLong.intValue() : 0;
                                    return CinemaResponse.from(cinema, totalRooms);
                                })
                        .collect(Collectors.toList());
        return CinemaListResponse.builder()
                .content(cinemaResponses)
                .page(cinemaPage.getNumber())
                .size(cinemaPage.getSize())
                .totalElements(cinemaPage.getTotalElements())
                .totalPages(cinemaPage.getTotalPages())
                .build();
    }

    public CinemaResponse getCinemaById(Long id) {
        Cinema cinema = findCinemaById(id);
        return CinemaResponse.from(cinema);
    }

    public CinemaListResponse getCinemasByBrand(String brand) {
        List<Cinema> cinemas = cinemaRepository.findByBrand(brand);
        return buildCinemaListResponse(cinemas);
    }

    public CinemaListResponse getCinemasByCity(String city) {
        List<Cinema> cinemas = cinemaRepository.findByCity(city);
        return buildCinemaListResponse(cinemas);
    }

    public CinemaListResponse getCinemasByCityAndDistrict(String city, String district) {
        List<Cinema> cinemas = cinemaRepository.findByCityAndDistrict(city, district);
        return buildCinemaListResponse(cinemas);
    }

    private CinemaListResponse buildCinemaListResponse(List<Cinema> cinemas) {
        List<CinemaResponse> cinemaResponses =
                cinemas.stream()
                        .map(
                                cinema -> {
                                    Long totalRoomsLong =
                                            roomRepository.countByCinemaId(cinema.getId());
                                    Integer totalRooms =
                                            totalRoomsLong != null ? totalRoomsLong.intValue() : 0;
                                    return CinemaResponse.from(cinema, totalRooms);
                                })
                        .collect(Collectors.toList());
        return CinemaListResponse.builder()
                .content(cinemaResponses)
                .page(0)
                .size(cinemaResponses.size())
                .totalElements(cinemaResponses.size())
                .totalPages(1)
                .build();
    }

    public List<ShowtimeResponse> getCinemaShowtimes(
            Long cinemaId, Long movieId, String date, String startDate, String endDate) {
        findCinemaById(cinemaId);
        return showtimeService.getShowtimesByCinemaId(cinemaId, movieId, date, startDate, endDate);
    }

    public List<RoomResponse> getRoomsByCinemaId(Long cinemaId) {
        findCinemaById(cinemaId);
        return roomRepository.findByCinemaId(cinemaId).stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    private Cinema findCinemaById(Long id) {
        return cinemaRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CINEMA_NOT_FOUND));
    }
}
