package com.ticket_online.domain.showtimes.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.domain.ShowtimeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ShowtimeResponse(
        Long id,
        Long movieId,
        String movieTitle,
        String moviePosterUrl,
        Integer movieDuration,
        String movieRating,
        Long cinemaId,
        String cinemaName,
        String cinemaAddress,
        Long screenId,
        String screenName,
        String screenType,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime,
        BigDecimal basePrice,
        ShowtimeStatus status,
        Integer availableSeats,
        Integer totalSeats) {

    public static ShowtimeResponse from(Showtime showtime) {
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovie().getId())
                .movieTitle(showtime.getMovie().getTitle())
                .moviePosterUrl(showtime.getMovie().getPosterUrl())
                .movieDuration(showtime.getMovie().getDuration())
                .movieRating(showtime.getMovie().getRating())
                .cinemaId(showtime.getCinema().getId())
                .cinemaName(showtime.getCinema().getName())
                .cinemaAddress(showtime.getCinema().getFullAddress())
                .screenId(showtime.getScreen().getId())
                .screenName(showtime.getScreen().getName())
                .screenType(showtime.getScreen().getType())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .status(showtime.getStatus())
                .availableSeats(0) // TODO: Calculate from bookings
                .totalSeats(showtime.getScreen().getTotalSeats())
                .build();
    }

    public static ShowtimeResponse from(Showtime showtime, int availableSeats) {
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovie().getId())
                .movieTitle(showtime.getMovie().getTitle())
                .moviePosterUrl(showtime.getMovie().getPosterUrl())
                .movieDuration(showtime.getMovie().getDuration())
                .movieRating(showtime.getMovie().getRating())
                .cinemaId(showtime.getCinema().getId())
                .cinemaName(showtime.getCinema().getName())
                .cinemaAddress(showtime.getCinema().getFullAddress())
                .screenId(showtime.getScreen().getId())
                .screenName(showtime.getScreen().getName())
                .screenType(showtime.getScreen().getType())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .status(showtime.getStatus())
                .availableSeats(availableSeats)
                .totalSeats(showtime.getScreen().getTotalSeats())
                .build();
    }
}
