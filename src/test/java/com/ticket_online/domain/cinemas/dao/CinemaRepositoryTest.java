package com.ticket_online.domain.cinemas.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticket_online.domain.cinemas.domain.Cinema;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class CinemaRepositoryTest {

    @Autowired private CinemaRepository cinemaRepository;

    private Cinema cgvVincom;
    private Cinema cgvLandmark;
    private Cinema lotteCongHoa;
    private Cinema galaxyNguyen;

    @BeforeEach
    void setUp() {
        // Create test data
        cgvVincom =
                Cinema.createCinema(
                        "CGV Vincom Center",
                        "CGV",
                        "logo1.png",
                        "72 Le Thanh Ton Street",
                        "District 1",
                        "Ho Chi Minh City",
                        "028-3822-3456",
                        "https://www.cgv.vn",
                        "Premium cinema with IMAX");

        cgvLandmark =
                Cinema.createCinema(
                        "CGV Landmark 81",
                        "CGV",
                        "logo2.png",
                        "Landmark 81",
                        "Binh Thanh District",
                        "Ho Chi Minh City",
                        "028-3822-3457",
                        "https://www.cgv.vn",
                        "Premium cinema");

        lotteCongHoa =
                Cinema.createCinema(
                        "Lotte Cinema Cong Hoa",
                        "Lotte",
                        "logo3.png",
                        "Cong Hoa Street",
                        "Tan Binh District",
                        "Ho Chi Minh City",
                        "028-3844-5678",
                        "https://www.lottevn.com",
                        "Modern cinema complex");

        galaxyNguyen =
                Cinema.createCinema(
                        "Galaxy Nguyen Du",
                        "Galaxy",
                        "logo4.png",
                        "116 Nguyen Du",
                        "District 1",
                        "Hanoi",
                        "024-3936-1234",
                        "https://www.galaxycine.vn",
                        "Popular cinema");

        cinemaRepository.saveAll(List.of(cgvVincom, cgvLandmark, lotteCongHoa, galaxyNguyen));
    }

    @AfterEach
    void tearDown() {
        cinemaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find all cinemas when no filters applied")
    void shouldFindAllCinemasWhenNoFiltersApplied() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Cinema> result = cinemaRepository.findByFilters(null, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("Should find cinemas by brand")
    void shouldFindCinemasByBrand() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Cinema> result = cinemaRepository.findByFilters("CGV", null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(cinema -> cinema.getBrand().equals("CGV"))
                .extracting(Cinema::getName)
                .containsExactlyInAnyOrder("CGV Vincom Center", "CGV Landmark 81");
    }

    @Test
    @DisplayName("Should find cinemas by city")
    void shouldFindCinemasByCity() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Cinema> result =
                cinemaRepository.findByFilters(null, "Ho Chi Minh City", null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .allMatch(cinema -> cinema.getCity().equals("Ho Chi Minh City"));
    }

    @Test
    @DisplayName("Should find cinemas by district")
    void shouldFindCinemasByDistrict() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Cinema> result = cinemaRepository.findByFilters(null, null, "District 1", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(cinema -> cinema.getDistrict().equals("District 1"))
                .extracting(Cinema::getName)
                .containsExactlyInAnyOrder("CGV Vincom Center", "Galaxy Nguyen Du");
    }

    @Test
    @DisplayName("Should find cinemas with multiple filters")
    void shouldFindCinemasWithMultipleFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Cinema> result =
                cinemaRepository.findByFilters("CGV", "Ho Chi Minh City", "District 1", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("CGV Vincom Center");
        assertThat(result.getContent().get(0).getBrand()).isEqualTo("CGV");
        assertThat(result.getContent().get(0).getCity()).isEqualTo("Ho Chi Minh City");
        assertThat(result.getContent().get(0).getDistrict()).isEqualTo("District 1");
    }

    @Test
    @DisplayName("Should return empty when filters match no cinemas")
    void shouldReturnEmptyWhenFiltersMatchNoCinemas() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Cinema> result =
                cinemaRepository.findByFilters("NonExistentBrand", null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should find cinemas by brand and city combination")
    void shouldFindCinemasByBrandAndCityCombination() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Cinema> result =
                cinemaRepository.findByFilters("CGV", "Ho Chi Minh City", null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(
                        cinema ->
                                cinema.getBrand().equals("CGV")
                                        && cinema.getCity().equals("Ho Chi Minh City"));
    }

    @Test
    @DisplayName("Should search cinemas by keyword in name")
    void shouldSearchCinemasByKeywordInName() {
        // Given
        String keyword = "Vincom";

        // When
        List<Cinema> result = cinemaRepository.searchByKeyword(keyword);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Vincom");
    }

    @Test
    @DisplayName("Should search cinemas by keyword in brand")
    void shouldSearchCinemasByKeywordInBrand() {
        // Given
        String keyword = "CGV";

        // When
        List<Cinema> result = cinemaRepository.searchByKeyword(keyword);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(cinema -> cinema.getBrand().equals("CGV"));
    }

    @Test
    @DisplayName("Should search cinemas case insensitively")
    void shouldSearchCinemasCaseInsensitively() {
        // Given
        String keyword = "lotte";

        // When
        List<Cinema> result = cinemaRepository.searchByKeyword(keyword);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("Lotte");
    }

    @Test
    @DisplayName("Should return empty list when keyword matches no cinemas")
    void shouldReturnEmptyListWhenKeywordMatchesNoCinemas() {
        // Given
        String keyword = "NonExistent";

        // When
        List<Cinema> result = cinemaRepository.searchByKeyword(keyword);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should search cinemas with partial keyword")
    void shouldSearchCinemasWithPartialKeyword() {
        // Given
        String keyword = "Land";

        // When
        List<Cinema> result = cinemaRepository.searchByKeyword(keyword);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Landmark");
    }

    @Test
    @DisplayName("Should find all distinct brands")
    void shouldFindAllDistinctBrands() {
        // When
        List<String> brands = cinemaRepository.findAllBrands();

        // Then
        assertThat(brands).isNotNull();
        assertThat(brands).hasSize(3);
        assertThat(brands).containsExactlyInAnyOrder("CGV", "Lotte", "Galaxy");
    }

    @Test
    @DisplayName("Should return brands in sorted order")
    void shouldReturnBrandsInSortedOrder() {
        // When
        List<String> brands = cinemaRepository.findAllBrands();

        // Then
        assertThat(brands).isNotNull();
        assertThat(brands).containsExactly("CGV", "Galaxy", "Lotte");
    }

    @Test
    @DisplayName("Should find all distinct cities")
    void shouldFindAllDistinctCities() {
        // When
        List<String> cities = cinemaRepository.findAllCities();

        // Then
        assertThat(cities).isNotNull();
        assertThat(cities).hasSize(2);
        assertThat(cities).containsExactlyInAnyOrder("Ho Chi Minh City", "Hanoi");
    }

    @Test
    @DisplayName("Should return cities in sorted order")
    void shouldReturnCitiesInSortedOrder() {
        // When
        List<String> cities = cinemaRepository.findAllCities();

        // Then
        assertThat(cities).isNotNull();
        assertThat(cities).containsExactly("Hanoi", "Ho Chi Minh City");
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        // When
        Page<Cinema> firstResult = cinemaRepository.findByFilters(null, null, null, firstPage);
        Page<Cinema> secondResult = cinemaRepository.findByFilters(null, null, null, secondPage);

        // Then
        assertThat(firstResult.getContent()).hasSize(2);
        assertThat(firstResult.getTotalElements()).isEqualTo(4);
        assertThat(firstResult.getTotalPages()).isEqualTo(2);
        assertThat(firstResult.getNumber()).isEqualTo(0);

        assertThat(secondResult.getContent()).hasSize(2);
        assertThat(secondResult.getTotalElements()).isEqualTo(4);
        assertThat(secondResult.getTotalPages()).isEqualTo(2);
        assertThat(secondResult.getNumber()).isEqualTo(1);

        // Verify different results on different pages
        assertThat(firstResult.getContent()).doesNotContainAnyElementsOf(secondResult.getContent());
    }

    @Test
    @DisplayName("Should return empty brands list when no cinemas exist")
    void shouldReturnEmptyBrandsListWhenNoCinemasExist() {
        // Given
        cinemaRepository.deleteAll();

        // When
        List<String> brands = cinemaRepository.findAllBrands();

        // Then
        assertThat(brands).isNotNull();
        assertThat(brands).isEmpty();
    }

    @Test
    @DisplayName("Should return empty cities list when no cinemas exist")
    void shouldReturnEmptyCitiesListWhenNoCinemasExist() {
        // Given
        cinemaRepository.deleteAll();

        // When
        List<String> cities = cinemaRepository.findAllCities();

        // Then
        assertThat(cities).isNotNull();
        assertThat(cities).isEmpty();
    }
}
