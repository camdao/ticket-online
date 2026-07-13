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

    private List<MovieResponse> movies;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
