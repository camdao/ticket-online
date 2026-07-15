package com.ticket_online.domain.seats.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/** Response DTO for screen layout information */
@Getter
@Builder
public class ScreenLayoutResponse {

    private List<String> rows;
    private Integer seatsPerRow;

    public static ScreenLayoutResponse of(List<String> rows, Integer seatsPerRow) {
        return ScreenLayoutResponse.builder().rows(rows).seatsPerRow(seatsPerRow).build();
    }
}
