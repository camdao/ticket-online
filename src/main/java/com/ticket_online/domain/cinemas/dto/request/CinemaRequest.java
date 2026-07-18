package com.ticket_online.domain.cinemas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating or updating a cinema")
public record CinemaRequest(
        @Schema(
                        description = "Name of the cinema",
                        example = "CGV Vincom Center",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Cinema name is required")
                @Size(max = 255, message = "Cinema name must not exceed 255 characters")
                String name,
        @Schema(
                        description = "Brand of the cinema chain",
                        example = "CGV",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Brand is required")
                @Size(max = 100, message = "Brand must not exceed 100 characters")
                String brand,
        @Schema(
                        description = "URL to the cinema brand logo",
                        example = "https://example.com/logo.png",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Size(max = 1000, message = "Logo URL must not exceed 1000 characters")
                String logoUrl,
        @Schema(
                        description = "Street address of the cinema",
                        example = "72 Le Thanh Ton Street",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Size(max = 500, message = "Address must not exceed 500 characters")
                String address,
        @Schema(
                        description = "District where the cinema is located",
                        example = "District 1",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Size(max = 100, message = "District must not exceed 100 characters")
                String district,
        @Schema(
                        description = "City where the cinema is located",
                        example = "Ho Chi Minh City",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Size(max = 100, message = "City must not exceed 100 characters")
                String city,
        @Schema(
                        description = "Contact phone number",
                        example = "0283822-3456",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Size(max = 20, message = "Phone must not exceed 20 characters")
                String phone,
        @Schema(
                        description = "Official website URL",
                        example = "https://www.cgv.vn",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Size(max = 500, message = "Website must not exceed 500 characters")
                String website,
        @Schema(
                        description = "Additional description or notes about the cinema",
                        example = "Premium cinema with IMAX and 4DX screens",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                String description) {}
