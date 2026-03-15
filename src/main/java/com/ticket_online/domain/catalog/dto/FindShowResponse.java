package com.ticket_online.domain.catalog.dto;

import java.time.LocalDateTime;

public record FindShowResponse(Long id, String name, LocalDateTime start_time, String location) {

    public static FindShowResponse of(
            Long id, String name, LocalDateTime start_time, String location) {
        return new FindShowResponse(id, name, start_time, location);
    }
}
