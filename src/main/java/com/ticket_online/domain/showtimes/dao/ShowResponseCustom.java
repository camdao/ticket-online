package com.ticket_online.domain.showtimes.dao;

import com.ticket_online.domain.showtimes.domain.Showtime;
import java.time.LocalDate;
import java.util.List;

public interface ShowResponseCustom {

    List<Showtime> findShowtimesWithFilters(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate);

    List<Showtime> findShowtimesByMovieId(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate);

    List<Showtime> findShowtimesByCinemaId(
            Long cinemaId, Long movieId, String date, String startDate, String endDate);

    List<LocalDate> findDistinctShowtimeDates(Long movieId, Long cinemaId);
}
