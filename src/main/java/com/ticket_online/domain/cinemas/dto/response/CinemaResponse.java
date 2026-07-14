package com.ticket_online.domain.cinemas.dto.response;

import com.ticket_online.domain.cinemas.domain.Cinema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CinemaResponse {
    private Long id;
    private String name;
    private String brand;
    private String logoUrl;
    private String address;
    private String district;
    private String city;
    private String phone;
    private String website;
    private String description;
    private Integer totalScreens;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CinemaResponse from(Cinema cinema) {
        return CinemaResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .brand(cinema.getBrand())
                .logoUrl(cinema.getLogoUrl())
                .address(cinema.getAddress())
                .district(cinema.getDistrict())
                .city(cinema.getCity())
                .phone(cinema.getPhone())
                .website(cinema.getWebsite())
                .description(cinema.getDescription())
                .totalScreens(null) // Will be populated when needed
                .createdAt(cinema.getCreatedAt())
                .updatedAt(cinema.getUpdatedAt())
                .build();
    }

    public static CinemaResponse from(Cinema cinema, Integer totalScreens) {
        return CinemaResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .brand(cinema.getBrand())
                .logoUrl(cinema.getLogoUrl())
                .address(cinema.getAddress())
                .district(cinema.getDistrict())
                .city(cinema.getCity())
                .phone(cinema.getPhone())
                .website(cinema.getWebsite())
                .description(cinema.getDescription())
                .totalScreens(totalScreens)
                .createdAt(cinema.getCreatedAt())
                .updatedAt(cinema.getUpdatedAt())
                .build();
    }
}
