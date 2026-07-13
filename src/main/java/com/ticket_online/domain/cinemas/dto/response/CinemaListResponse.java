package com.ticket_online.domain.cinemas.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CinemaListResponse {
    private List<CinemaResponse> cinemas;
    private int totalCount;

    public static CinemaListResponse of(List<CinemaResponse> cinemas) {
        return CinemaListResponse.builder().cinemas(cinemas).totalCount(cinemas.size()).build();
    }
}
