package com.ticket_online.domain.cinemas.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaListResponse {
    private List<CinemaResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
