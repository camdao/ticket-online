package com.ticket_online.domain.cinemas.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticket_online.domain.rooms.Room;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response containing room information")
public record RoomResponse(
        @Schema(description = "Unique identifier of the room", example = "1") Long id,
        @Schema(description = "ID of the cinema this room belongs to", example = "1") Long cinemaId,
        @Schema(description = "Name or number of the room", example = "Room 1") String name,
        @Schema(description = "Total seating capacity", example = "150") Integer capacity,
        @Schema(description = "Type of room (Standard, IMAX, VIP, 4DX)", example = "IMAX")
                String roomType,
        @Schema(description = "Timestamp when the room was created") @JsonProperty("createdAt")
                LocalDateTime createdAt,
        @Schema(description = "Timestamp when the room was last updated") @JsonProperty("updatedAt")
                LocalDateTime updatedAt) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getCinemaId(),
                room.getName(),
                room.getCapacity(),
                room.getRoomType(),
                room.getCreatedAt(),
                room.getUpdatedAt());
    }
}
