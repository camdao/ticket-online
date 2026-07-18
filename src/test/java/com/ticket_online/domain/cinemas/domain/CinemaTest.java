package com.ticket_online.domain.cinemas.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CinemaTest {

    @Test
    @DisplayName("Should create cinema with all fields")
    void shouldCreateCinemaWithAllFields() {
        // Given
        String name = "CGV Vincom Center";
        String brand = "CGV";
        String logoUrl = "https://example.com/logo.png";
        String address = "72 Le Thanh Ton Street";
        String district = "District 1";
        String city = "Ho Chi Minh City";
        String phone = "0283822-3456";
        String website = "https://www.cgv.vn";
        String description = "Premium cinema with IMAX";

        // When
        Cinema cinema =
                Cinema.createCinema(
                        name, brand, logoUrl, address, district, city, phone, website, description);

        // Then
        assertThat(cinema.getName()).isEqualTo(name);
        assertThat(cinema.getBrand()).isEqualTo(brand);
        assertThat(cinema.getLogoUrl()).isEqualTo(logoUrl);
        assertThat(cinema.getAddress()).isEqualTo(address);
        assertThat(cinema.getDistrict()).isEqualTo(district);
        assertThat(cinema.getCity()).isEqualTo(city);
        assertThat(cinema.getPhone()).isEqualTo(phone);
        assertThat(cinema.getWebsite()).isEqualTo(website);
        assertThat(cinema.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("Should create cinema with minimal fields")
    void shouldCreateCinemaWithMinimalFields() {
        // Given
        String name = "CGV Vincom Center";
        String brand = "CGV";

        // When
        Cinema cinema = Cinema.createCinema(name, brand, null, null, null, null, null, null, null);

        // Then
        assertThat(cinema.getName()).isEqualTo(name);
        assertThat(cinema.getBrand()).isEqualTo(brand);
        assertThat(cinema.getLogoUrl()).isNull();
        assertThat(cinema.getAddress()).isNull();
        assertThat(cinema.getDistrict()).isNull();
        assertThat(cinema.getCity()).isNull();
        assertThat(cinema.getPhone()).isNull();
        assertThat(cinema.getWebsite()).isNull();
        assertThat(cinema.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should return full address with all location fields")
    void shouldReturnFullAddressWithAllLocationFields() {
        // Given
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

        // When
        String fullAddress = cinema.getFullAddress();

        // Then
        assertThat(fullAddress).isEqualTo("72 Le Thanh Ton Street, District 1, Ho Chi Minh City");
    }

    @Test
    @DisplayName("Should return full address with only address")
    void shouldReturnFullAddressWithOnlyAddress() {
        // Given
        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom Center",
                        "CGV",
                        null,
                        "72 Le Thanh Ton Street",
                        null,
                        null,
                        null,
                        null,
                        null);

        // When
        String fullAddress = cinema.getFullAddress();

        // Then
        assertThat(fullAddress).isEqualTo("72 Le Thanh Ton Street");
    }

    @Test
    @DisplayName("Should return full address with address and district")
    void shouldReturnFullAddressWithAddressAndDistrict() {
        // Given
        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom Center",
                        "CGV",
                        null,
                        "72 Le Thanh Ton Street",
                        "District 1",
                        null,
                        null,
                        null,
                        null);

        // When
        String fullAddress = cinema.getFullAddress();

        // Then
        assertThat(fullAddress).isEqualTo("72 Le Thanh Ton Street, District 1");
    }

    @Test
    @DisplayName("Should return full address with district and city only")
    void shouldReturnFullAddressWithDistrictAndCityOnly() {
        // Given
        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom Center",
                        "CGV",
                        null,
                        null,
                        "District 1",
                        "Ho Chi Minh City",
                        null,
                        null,
                        null);

        // When
        String fullAddress = cinema.getFullAddress();

        // Then
        assertThat(fullAddress).isEqualTo(", District 1, Ho Chi Minh City");
    }

    @Test
    @DisplayName("Should return empty string when all location fields are null")
    void shouldReturnEmptyStringWhenAllLocationFieldsAreNull() {
        // Given
        Cinema cinema =
                Cinema.createCinema(
                        "CGV Vincom Center", "CGV", null, null, null, null, null, null, null);

        // When
        String fullAddress = cinema.getFullAddress();

        // Then
        assertThat(fullAddress).isEmpty();
    }

    @Test
    @DisplayName("Should create cinema with different brands")
    void shouldCreateCinemaWithDifferentBrands() {
        // Given & When
        Cinema cgv =
                Cinema.createCinema(
                        "CGV Cinema",
                        "CGV",
                        null,
                        "Address 1",
                        "District 1",
                        "HCMC",
                        null,
                        null,
                        null);
        Cinema lotte =
                Cinema.createCinema(
                        "Lotte Cinema",
                        "Lotte",
                        null,
                        "Address 2",
                        "District 2",
                        "HCMC",
                        null,
                        null,
                        null);
        Cinema galaxy =
                Cinema.createCinema(
                        "Galaxy Cinema",
                        "Galaxy",
                        null,
                        "Address 3",
                        "District 3",
                        "HCMC",
                        null,
                        null,
                        null);

        // Then
        assertThat(cgv.getBrand()).isEqualTo("CGV");
        assertThat(lotte.getBrand()).isEqualTo("Lotte");
        assertThat(galaxy.getBrand()).isEqualTo("Galaxy");
    }
}
