package com.ticket_online.domain.cinemas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticket_online.domain.cinemas.dao.CinemaRepository;
import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.cinemas.dto.response.CinemaListResponse;
import com.ticket_online.domain.cinemas.dto.response.CinemaResponse;
import com.ticket_online.domain.rooms.RoomRepository;
import com.ticket_online.domain.showtimes.application.ShowtimeService;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.lang.reflect.Field;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock private CinemaRepository cinemaRepository;

    @Mock private RoomRepository roomRepository;

    @Mock private ShowtimeService showtimeService;

    @InjectMocks private CinemaService cinemaService;

    private void setId(Cinema cinema, Long id) {
        try {
            Field idField = Cinema.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(cinema, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set cinema ID", e);
        }
    }

    @Test
    @DisplayName("Should return cinema by id when cinema exists")
    void shouldReturnCinemaByIdWhenCinemaExists() {
        // Given
        Long cinemaId = 1L;
        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom Center",
                        "CGV",
                        "logo.png",
                        "72 Le Thanh Ton Street",
                        "District 1",
                        "Ho Chi Minh City",
                        "0283822-3456",
                        "https://www.cgv.vn",
                        "Premium cinema");
        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.of(cinema));

        // When
        CinemaResponse result = cinemaService.getCinemaById(cinemaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("CGV Vincom Center");
        assertThat(result.brand()).isEqualTo("CGV");
        assertThat(result.city()).isEqualTo("Ho Chi Minh City");
        verify(cinemaRepository).findById(cinemaId);
    }

    @Test
    @DisplayName("Should throw exception when cinema not found")
    void shouldThrowExceptionWhenCinemaNotFound() {
        // Given
        Long cinemaId = 999L;
        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cinemaService.getCinemaById(cinemaId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CINEMA_NOT_FOUND);
        verify(cinemaRepository).findById(cinemaId);
    }

    @Test
    @DisplayName("Should return all cinemas with pagination and no filters")
    void shouldReturnAllCinemasWithPaginationAndNoFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Cinema> cinemas =
                Arrays.asList(
                        Cinema.createCinema(
                                "CGV Vincom",
                                "CGV",
                                "logo1.png",
                                "Address 1",
                                "District 1",
                                "HCMC",
                                "123456",
                                "www.cgv.vn",
                                "Desc 1"),
                        Cinema.createCinema(
                                "Lotte Cinema",
                                "Lotte",
                                "logo2.png",
                                "Address 2",
                                "District 2",
                                "HCMC",
                                "654321",
                                "www.lotte.vn",
                                "Desc 2"));
        Page<Cinema> cinemaPage = new PageImpl<>(cinemas, pageable, cinemas.size());

        List<Object[]> roomCounts =
                Arrays.<Object[]>asList(new Object[] {1L, 8L}, new Object[] {2L, 10L});

        when(cinemaRepository.findByFilters(null, null, null, pageable)).thenReturn(cinemaPage);
        when(roomRepository.countByCinemaIds(anyList())).thenReturn(roomCounts);

        // When
        CinemaListResponse result = cinemaService.getCinemas(pageable, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.content().get(0).name()).isEqualTo("CGV Vincom");
        assertThat(result.content().get(1).name()).isEqualTo("Lotte Cinema");
        verify(cinemaRepository).findByFilters(null, null, null, pageable);
    }

    @Test
    @DisplayName("Should return cinemas filtered by brand")
    void shouldReturnCinemasFilteredByBrand() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        String brand = "CGV";
        List<Cinema> cinemas =
                Arrays.asList(
                        Cinema.createCinema(
                                "CGV Vincom",
                                "CGV",
                                "logo.png",
                                "Address 1",
                                "District 1",
                                "HCMC",
                                "123456",
                                "www.cgv.vn",
                                "Desc"));
        Page<Cinema> cinemaPage = new PageImpl<>(cinemas, pageable, cinemas.size());

        List<Object[]> roomCounts = Collections.singletonList(new Object[] {1L, 8L});

        when(cinemaRepository.findByFilters(brand, null, null, pageable)).thenReturn(cinemaPage);
        when(roomRepository.countByCinemaIds(anyList())).thenReturn(roomCounts);

        // When
        CinemaListResponse result = cinemaService.getCinemas(pageable, brand, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).brand()).isEqualTo("CGV");
        verify(cinemaRepository).findByFilters(brand, null, null, pageable);
    }

    @Test
    @DisplayName("Should return cinemas filtered by city")
    void shouldReturnCinemasFilteredByCity() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        String city = "Ho Chi Minh City";
        List<Cinema> cinemas =
                Arrays.asList(
                        Cinema.createCinema(
                                "CGV Vincom",
                                "CGV",
                                "logo.png",
                                "Address 1",
                                "District 1",
                                "Ho Chi Minh City",
                                "123456",
                                "www.cgv.vn",
                                "Desc"));
        Page<Cinema> cinemaPage = new PageImpl<>(cinemas, pageable, cinemas.size());

        List<Object[]> roomCounts = Collections.singletonList(new Object[] {1L, 8L});

        when(cinemaRepository.findByFilters(null, city, null, pageable)).thenReturn(cinemaPage);
        when(roomRepository.countByCinemaIds(anyList())).thenReturn(roomCounts);

        // When
        CinemaListResponse result = cinemaService.getCinemas(pageable, null, city, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).city()).isEqualTo("Ho Chi Minh City");
        verify(cinemaRepository).findByFilters(null, city, null, pageable);
    }

    @Test
    @DisplayName("Should return cinemas filtered by district")
    void shouldReturnCinemasFilteredByDistrict() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        String district = "District 1";
        List<Cinema> cinemas =
                Arrays.asList(
                        Cinema.createCinema(
                                "CGV Vincom",
                                "CGV",
                                "logo.png",
                                "Address 1",
                                "District 1",
                                "HCMC",
                                "123456",
                                "www.cgv.vn",
                                "Desc"));
        Page<Cinema> cinemaPage = new PageImpl<>(cinemas, pageable, cinemas.size());

        List<Object[]> roomCounts = Collections.singletonList(new Object[] {1L, 8L});

        when(cinemaRepository.findByFilters(null, null, district, pageable)).thenReturn(cinemaPage);
        when(roomRepository.countByCinemaIds(anyList())).thenReturn(roomCounts);

        // When
        CinemaListResponse result = cinemaService.getCinemas(pageable, null, null, district);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).district()).isEqualTo("District 1");
        verify(cinemaRepository).findByFilters(null, null, district, pageable);
    }

    @Test
    @DisplayName("Should return cinemas with multiple filters")
    void shouldReturnCinemasWithMultipleFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        String brand = "CGV";
        String city = "Ho Chi Minh City";
        String district = "District 1";
        List<Cinema> cinemas =
                Arrays.asList(
                        Cinema.createCinema(
                                "CGV Vincom",
                                "CGV",
                                "logo.png",
                                "Address 1",
                                "District 1",
                                "Ho Chi Minh City",
                                "123456",
                                "www.cgv.vn",
                                "Desc"));
        Page<Cinema> cinemaPage = new PageImpl<>(cinemas, pageable, cinemas.size());

        List<Object[]> roomCounts = Collections.singletonList(new Object[] {1L, 8L});

        when(cinemaRepository.findByFilters(brand, city, district, pageable))
                .thenReturn(cinemaPage);
        when(roomRepository.countByCinemaIds(anyList())).thenReturn(roomCounts);

        // When
        CinemaListResponse result = cinemaService.getCinemas(pageable, brand, city, district);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).brand()).isEqualTo("CGV");
        assertThat(result.content().get(0).city()).isEqualTo("Ho Chi Minh City");
        assertThat(result.content().get(0).district()).isEqualTo("District 1");
        verify(cinemaRepository).findByFilters(brand, city, district, pageable);
    }

    @Test
    @DisplayName("Should return empty list when no cinemas match filters")
    void shouldReturnEmptyListWhenNoCinemasMatchFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        String brand = "NonExistentBrand";
        Page<Cinema> cinemaPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(cinemaRepository.findByFilters(brand, null, null, pageable)).thenReturn(cinemaPage);
        when(roomRepository.countByCinemaIds(anyList())).thenReturn(Collections.emptyList());

        // When
        CinemaListResponse result = cinemaService.getCinemas(pageable, brand, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
        verify(cinemaRepository).findByFilters(brand, null, null, pageable);
    }

    @Test
    @DisplayName("Should include room count in cinema response")
    void shouldIncludeRoomCountInCinemaResponse() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom",
                        "CGV",
                        "logo.png",
                        "Address 1",
                        "District 1",
                        "HCMC",
                        "123456",
                        "www.cgv.vn",
                        "Desc");
        setId(cinema, 1L);
        List<Cinema> cinemas = Arrays.asList(cinema);
        Page<Cinema> cinemaPage = new PageImpl<>(cinemas, pageable, cinemas.size());

        List<Object[]> roomCounts = Collections.singletonList(new Object[] {1L, 12L});

        when(cinemaRepository.findByFilters(null, null, null, pageable)).thenReturn(cinemaPage);
        when(roomRepository.countByCinemaIds(anyList())).thenReturn(roomCounts);

        // When
        CinemaListResponse result = cinemaService.getCinemas(pageable, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).totalScreens()).isEqualTo(12);
    }

    @Test
    @DisplayName("Should return zero room count when cinema has no rooms")
    void shouldReturnZeroRoomCountWhenCinemaHasNoRooms() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<Cinema> cinemas =
                Arrays.asList(
                        Cinema.createCinema(
                                "New Cinema",
                                "CGV",
                                "logo.png",
                                "Address 1",
                                "District 1",
                                "HCMC",
                                "123456",
                                "www.cgv.vn",
                                "Desc"));
        Page<Cinema> cinemaPage = new PageImpl<>(cinemas, pageable, cinemas.size());

        when(cinemaRepository.findByFilters(null, null, null, pageable)).thenReturn(cinemaPage);
        when(roomRepository.countByCinemaIds(anyList())).thenReturn(Collections.emptyList());

        // When
        CinemaListResponse result = cinemaService.getCinemas(pageable, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).totalScreens()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should throw exception when getting showtimes for non-existent cinema")
    void shouldThrowExceptionWhenGettingShowtimesForNonExistentCinema() {
        // Given
        Long cinemaId = 999L;
        when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cinemaService.getCinemaShowtimes(cinemaId, null, null, null, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CINEMA_NOT_FOUND);
        verify(cinemaRepository).findById(cinemaId);
    }

    @Test
    @DisplayName("Should handle pagination with different page sizes")
    void shouldHandlePaginationWithDifferentPageSizes() {
        // Given
        Pageable pageable = PageRequest.of(1, 10);
        List<Cinema> cinemas =
                Arrays.asList(
                        Cinema.createCinema(
                                "Cinema 11",
                                "CGV",
                                null,
                                "Address",
                                "District 1",
                                "HCMC",
                                null,
                                null,
                                null));
        Page<Cinema> cinemaPage = new PageImpl<>(cinemas, pageable, 25);

        List<Object[]> roomCounts = Collections.singletonList(new Object[] {1L, 5L});

        when(cinemaRepository.findByFilters(null, null, null, pageable)).thenReturn(cinemaPage);
        when(roomRepository.countByCinemaIds(anyList())).thenReturn(roomCounts);

        // When
        CinemaListResponse result = cinemaService.getCinemas(pageable, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(25);
        assertThat(result.totalPages()).isEqualTo(3);
    }
}
