package com.ticket_online.domain.cinemas.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScreenRequest {

    @NotNull(message = "Cinema ID is required")
    private Long cinemaId;

    @NotBlank(message = "Screen name is required")
    @Size(max = 100, message = "Screen name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Size(max = 50, message = "Room type must not exceed 50 characters")
    private String roomType;
}
