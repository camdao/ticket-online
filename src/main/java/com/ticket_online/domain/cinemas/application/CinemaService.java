package com.ticket_online.domain.cinemas.application;

import com.ticket_online.domain.cinemas.dao.CinemaRepository;
import com.ticket_online.domain.cinemas.dao.ScreenRepository;
import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.cinemas.domain.Screen;
import com.ticket_online.domain.cinemas.dto.request.CinemaRequest;
import com.ticket_online.domain.cinemas.dto.response.CinemaDetailResponse;
import com.ticket_online.domain.cinemas.dto.response.CinemaListResponse;
import com.ticket_online.domain.cinemas.dto.response.CinemaResponse;
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
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;

    public CinemaListResponse getAllCinemas() {
        List<CinemaResponse> cinemas =
                cinemaRepository.findAll().stream()
                        .map(CinemaResponse::from)
                        .collect(Collectors.toList());
        return CinemaListResponse.of(cinemas);
    }

    public CinemaResponse getCinemaById(Long id) {
        Cinema cinema = findCinemaById(id);
        return CinemaResponse.from(cinema);
    }

    public CinemaDetailResponse getCinemaDetail(Long id) {
        Cinema cinema = findCinemaById(id);
        List<Screen> screens = screenRepository.findByCinemaId(id);
        List<ScreenResponse> screenResponses =
                screens.stream().map(ScreenResponse::from).collect(Collectors.toList());
        Integer totalCapacity = screenRepository.getTotalCapacityByCinemaId(id);
        return CinemaDetailResponse.from(
                cinema, screenResponses, totalCapacity != null ? totalCapacity : 0);
    }

    public CinemaListResponse getCinemasByBrand(String brand) {
        List<CinemaResponse> cinemas =
                cinemaRepository.findByBrand(brand).stream()
                        .map(CinemaResponse::from)
                        .collect(Collectors.toList());
        return CinemaListResponse.of(cinemas);
    }

    public CinemaListResponse getCinemasByCity(String city) {
        List<CinemaResponse> cinemas =
                cinemaRepository.findByCity(city).stream()
                        .map(CinemaResponse::from)
                        .collect(Collectors.toList());
        return CinemaListResponse.of(cinemas);
    }

    public CinemaListResponse getCinemasByCityAndDistrict(String city, String district) {
        List<CinemaResponse> cinemas =
                cinemaRepository.findByCityAndDistrict(city, district).stream()
                        .map(CinemaResponse::from)
                        .collect(Collectors.toList());
        return CinemaListResponse.of(cinemas);
    }

    public CinemaListResponse searchCinemas(String keyword) {
        List<CinemaResponse> cinemas =
                cinemaRepository.searchByKeyword(keyword).stream()
                        .map(CinemaResponse::from)
                        .collect(Collectors.toList());
        return CinemaListResponse.of(cinemas);
    }

    public List<String> getAllBrands() {
        return cinemaRepository.findAllBrands();
    }

    public List<String> getAllCities() {
        return cinemaRepository.findAllCities();
    }

    @Transactional
    public CinemaResponse createCinema(CinemaRequest request) {
        Cinema cinema =
                Cinema.createCinema(
                        request.getName(),
                        request.getBrand(),
                        request.getLogoUrl(),
                        request.getAddress(),
                        request.getDistrict(),
                        request.getCity(),
                        request.getPhone(),
                        request.getWebsite(),
                        request.getDescription());
        Cinema savedCinema = cinemaRepository.save(cinema);
        return CinemaResponse.from(savedCinema);
    }

    @Transactional
    public CinemaResponse updateCinema(Long id, CinemaRequest request) {
        Cinema cinema = findCinemaById(id);
        cinema.updateCinema(
                request.getName(),
                request.getBrand(),
                request.getLogoUrl(),
                request.getAddress(),
                request.getDistrict(),
                request.getCity(),
                request.getPhone(),
                request.getWebsite(),
                request.getDescription());
        Cinema updatedCinema = cinemaRepository.save(cinema);
        return CinemaResponse.from(updatedCinema);
    }

    @Transactional
    public void deleteCinema(Long id) {
        Cinema cinema = findCinemaById(id);
        cinemaRepository.delete(cinema);
    }

    public List<ScreenResponse> getScreensByCinemaId(Long cinemaId) {
        findCinemaById(cinemaId);
        return screenRepository.findByCinemaId(cinemaId).stream()
                .map(ScreenResponse::from)
                .collect(Collectors.toList());
    }

    private Cinema findCinemaById(Long id) {
        return cinemaRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CINEMA_NOT_FOUND));
    }
}
