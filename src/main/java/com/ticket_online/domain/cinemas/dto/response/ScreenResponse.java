package com.ticket_online.domain.cinemas.dto.response;

import com.ticket_online.domain.cinemas.domain.Screen;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ScreenResponse {
    private Long id;
    private Long cinemaId;
    private String name;
    private Integer capacity;
    private String roomType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScreenResponse from(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .cinemaId(screen.getCinemaId())
                .name(screen.getName())
                .capacity(screen.getCapacity())
                .roomType(screen.getRoomType())
                .createdAt(screen.getCreatedAt())
                .updatedAt(screen.getUpdatedAt())
                .build();
    }
}
