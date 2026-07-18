package com.ticket_online.domain.cinemas.application;

import com.ticket_online.domain.cinemas.dao.CinemaRepository;
import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.cinemas.dto.response.CinemaListResponse;
import com.ticket_online.domain.cinemas.dto.response.CinemaResponse;
import com.ticket_online.domain.rooms.RoomRepository;
import com.ticket_online.domain.showtimes.application.ShowtimeService;
import com.ticket_online.domain.showtimes.dto.response.ShowtimeResponse;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.util.List;
import java.util.Map;
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

    public CinemaListResponse getCinemas(
            Pageable pageable, String brand, String city, String district) {
        Page<Cinema> cinemaPage = cinemaRepository.findByFilters(brand, city, district, pageable);

        List<Long> cinemaIds =
                cinemaPage.getContent().stream().map(Cinema::getId).collect(Collectors.toList());

        Map<Long, Integer> roomCountMap =
                roomRepository.countByCinemaIds(cinemaIds).stream()
                        .collect(
                                Collectors.toMap(
                                        row -> (Long) row[0], row -> ((Long) row[1]).intValue()));

        return CinemaListResponse.of(cinemaPage, roomCountMap);
    }

    public CinemaResponse getCinemaById(Long id) {
        Cinema cinema = findCinemaById(id);
        return CinemaResponse.from(cinema);
    }

    public List<ShowtimeResponse> getCinemaShowtimes(
            Long cinemaId, Long movieId, String date, String startDate, String endDate) {
        findCinemaById(cinemaId);
        return showtimeService.getShowtimesByCinemaId(cinemaId, movieId, date, startDate, endDate);
    }

    private Cinema findCinemaById(Long id) {
        return cinemaRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CINEMA_NOT_FOUND));
    }
}
