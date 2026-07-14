package com.ticket_online.domain.cinemas.dto.response;

import com.ticket_online.domain.cinemas.domain.Room;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private Long cinemaId;
    private String name;
    private Integer capacity;
    private String roomType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RoomResponse from(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .cinemaId(room.getCinemaId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .roomType(room.getRoomType())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
