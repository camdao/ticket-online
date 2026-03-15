package com.ticket_online.domain.catalog.dto.response;

import com.ticket_online.domain.catalog.domain.Show;
import java.time.LocalDateTime;

public record CreateShowResponse(Long id, String name, LocalDateTime startTime, String location) {
    public static CreateShowResponse from(Show show) {
        return new CreateShowResponse(
                show.getId(), show.getName(), show.getStart_time(), show.getLocation());
    }
}
