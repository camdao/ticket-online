package com.ticket_online.domain.cinemas.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticket_online.domain.cinemas.domain.Cinema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response containing cinema information")
public record CinemaResponse(
        @Schema(description = "Unique identifier of the cinema", example = "1") Long id,
        @Schema(description = "Name of the cinema", example = "CGV Vincom Center") String name,
        @Schema(description = "Brand of the cinema chain", example = "CGV") String brand,
        @Schema(
                        description = "URL to the cinema brand logo",
                        example = "https://example.com/logo.png")
                String logoUrl,
        @Schema(description = "Street address", example = "72 Le Thanh Ton Street") String address,
        @Schema(description = "District", example = "District 1") String district,
        @Schema(description = "City", example = "Ho Chi Minh City") String city,
        @Schema(description = "Contact phone number", example = "0283822-3456") String phone,
        @Schema(description = "Official website URL", example = "https://www.cgv.vn")
                String website,
        @Schema(description = "Additional description", example = "Premium cinema with IMAX")
                String description,
        @Schema(description = "Total number of screens/rooms", example = "8") Integer totalScreens,
        @Schema(description = "Timestamp when the cinema was created") @JsonProperty("createdAt")
                LocalDateTime createdAt,
        @Schema(description = "Timestamp when the cinema was last updated")
                @JsonProperty("updatedAt")
                LocalDateTime updatedAt) {

    public static CinemaResponse from(Cinema cinema) {
        return new CinemaResponse(
                cinema.getId(),
                cinema.getName(),
                cinema.getBrand(),
                cinema.getLogoUrl(),
                cinema.getAddress(),
                cinema.getDistrict(),
                cinema.getCity(),
                cinema.getPhone(),
                cinema.getWebsite(),
                cinema.getDescription(),
                null, // Will be populated when needed
                cinema.getCreatedAt(),
                cinema.getUpdatedAt());
    }

    public static CinemaResponse from(Cinema cinema, Integer totalScreens) {
        return new CinemaResponse(
                cinema.getId(),
                cinema.getName(),
                cinema.getBrand(),
                cinema.getLogoUrl(),
                cinema.getAddress(),
                cinema.getDistrict(),
                cinema.getCity(),
                cinema.getPhone(),
                cinema.getWebsite(),
                cinema.getDescription(),
                totalScreens,
                cinema.getCreatedAt(),
                cinema.getUpdatedAt());
    }
}
