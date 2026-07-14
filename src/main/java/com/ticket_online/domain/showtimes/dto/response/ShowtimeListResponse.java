package com.ticket_online.domain.showtimes.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record ShowtimeListResponse(
        List<ShowtimeResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static ShowtimeListResponse of(
            List<ShowtimeResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last) {
        return ShowtimeListResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(first)
                .last(last)
                .build();
    }
}
