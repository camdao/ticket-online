package com.ticket_online.domain.cinemas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CinemaRequest {

    @NotBlank(message = "Cinema name is required")
    @Size(max = 255, message = "Cinema name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Brand is required")
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Size(max = 1000, message = "Logo URL must not exceed 1000 characters")
    private String logoUrl;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 500, message = "Website must not exceed 500 characters")
    private String website;

    private String description;
}
