package com.ticket_online.domain.cinemas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating or updating a room")
public record RoomRequest(
        @Schema(
                        description = "ID of the cinema this room belongs to",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "Cinema ID is required")
                Long cinemaId,
        @Schema(
                        description = "Name or number of the room",
                        example = "Room 1",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Room name is required")
                @Size(max = 100, message = "Room name must not exceed 100 characters")
                String name,
        @Schema(
                        description = "Total seating capacity of the room",
                        example = "150",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "Capacity is required")
                @Min(value = 1, message = "Capacity must be at least 1")
                Integer capacity,
        @Schema(
                        description = "Type of room (e.g., Standard, IMAX, VIP, 4DX)",
                        example = "IMAX",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Size(max = 50, message = "Room type must not exceed 50 characters")
                String roomType) {}
