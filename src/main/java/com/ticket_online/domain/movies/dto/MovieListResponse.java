package com.ticket_online.domain.movies.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieListResponse {

    private List<MovieResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
