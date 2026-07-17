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
        MovieInfo movie,
        CinemaInfo cinema,
        ScreenInfo screen,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime,
        BigDecimal basePrice,
        ShowtimeStatus status,
        Integer availableSeats,
        Integer totalSeats) {

    @Builder
    public record MovieInfo(
            Long id, String title, Integer duration, String posterUrl, String ageRating) {}

    @Builder
    public record CinemaInfo(Long id, String name, String address, String phoneNumber) {}

    @Builder
    public record ScreenInfo(Long id, String name, String type, Integer totalSeats) {}

    public static ShowtimeDetailResponse from(Showtime showtime) {
        return ShowtimeDetailResponse.builder()
                .id(showtime.getId())
                .movie(
                        MovieInfo.builder()
                                .id(showtime.getMovie().getId())
                                .title(showtime.getMovie().getTitle())
                                .duration(showtime.getMovie().getDuration())
                                .posterUrl(showtime.getMovie().getImageUrl())
                                .ageRating(showtime.getMovie().getRating())
                                .build())
                .cinema(
                        CinemaInfo.builder()
                                .id(showtime.getCinema().getId())
                                .name(showtime.getCinema().getName())
                                .address(showtime.getCinema().getFullAddress())
                                .phoneNumber(showtime.getCinema().getPhone())
                                .build())
                .screen(
                        ScreenInfo.builder()
                                .id(showtime.getRoom().getId())
                                .name(showtime.getRoom().getName())
                                .type(showtime.getRoom().getType())
                                .totalSeats(showtime.getRoom().getTotalSeats())
                                .build())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .status(showtime.getStatus())
                .availableSeats(0)
                .build();
    }

    public static ShowtimeDetailResponse from(Showtime showtime, int availableSeats) {
        return ShowtimeDetailResponse.builder()
                .id(showtime.getId())
                .movie(
                        MovieInfo.builder()
                                .id(showtime.getMovie().getId())
                                .title(showtime.getMovie().getTitle())
                                .duration(showtime.getMovie().getDuration())
                                .posterUrl(showtime.getMovie().getImageUrl())
                                .ageRating(showtime.getMovie().getRating())
                                .build())
                .cinema(
                        CinemaInfo.builder()
                                .id(showtime.getCinema().getId())
                                .name(showtime.getCinema().getName())
                                .address(showtime.getCinema().getFullAddress())
                                .phoneNumber(showtime.getCinema().getPhone())
                                .build())
                .screen(
                        ScreenInfo.builder()
                                .id(showtime.getRoom().getId())
                                .name(showtime.getRoom().getName())
                                .type(showtime.getRoom().getType())
                                .totalSeats(showtime.getRoom().getTotalSeats())
                                .build())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .status(showtime.getStatus())
                .availableSeats(availableSeats)
                .build();
    }
}
