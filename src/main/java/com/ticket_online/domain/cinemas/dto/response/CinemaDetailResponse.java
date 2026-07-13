package com.ticket_online.domain.cinemas.dto.response;

import com.ticket_online.domain.cinemas.domain.Cinema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CinemaDetailResponse {
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
    private List<ScreenResponse> screens;
    private Integer totalScreens;
    private Integer totalCapacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CinemaDetailResponse from(
            Cinema cinema, List<ScreenResponse> screens, Integer totalCapacity) {
        return CinemaDetailResponse.builder()
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
                .screens(screens)
                .totalScreens(screens.size())
                .totalCapacity(totalCapacity)
                .createdAt(cinema.getCreatedAt())
                .updatedAt(cinema.getUpdatedAt())
                .build();
    }
}
