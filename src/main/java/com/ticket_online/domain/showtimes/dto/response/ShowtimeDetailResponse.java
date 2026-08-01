package com.ticket_online.domain.showtimes.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.domain.ShowtimeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ShowtimeDetailResponse(
        Long id,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime,
        BigDecimal basePrice,
        ShowtimeStatus status,
        Integer availableSeats,
        Integer totalSeats,
        MovieInfo movie,
        CinemaInfo cinema) {

    @Builder
    public record MovieInfo(
            Long id,
            String title,
            Integer duration,
            String genre,
            String ageRating,
            String posterUrl) {}

    @Builder
    public record CinemaInfo(
            Long id, String name, String brand, String address, String phoneNumber) {}

    public static ShowtimeDetailResponse from(
            Showtime showtime, Integer availableSeats, Integer totalSeats) {
        return ShowtimeDetailResponse.builder()
                .id(showtime.getId())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .status(showtime.getStatus())
                .availableSeats(availableSeats)
                .totalSeats(totalSeats)
                .movie(
                        MovieInfo.builder()
                                .id(showtime.getMovie().getId())
                                .title(showtime.getMovie().getTitle())
                                .duration(showtime.getMovie().getDuration())
                                .genre(showtime.getMovie().getGenre())
                                .ageRating(showtime.getMovie().getRating())
                                .posterUrl(showtime.getMovie().getImageUrl())
                                .build())
                .cinema(
                        CinemaInfo.builder()
                                .id(showtime.getCinema().getId())
                                .name(showtime.getCinema().getName())
                                .brand(showtime.getCinema().getBrand())
                                .address(showtime.getCinema().getFullAddress())
                                .phoneNumber(showtime.getCinema().getPhone())
                                .build())
                .build();
    }
}
