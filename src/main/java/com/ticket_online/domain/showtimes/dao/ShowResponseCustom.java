package com.ticket_online.domain.showtimes.dao;

import com.ticket_online.domain.showtimes.domain.Showtime;
import java.util.List;

public interface ShowResponseCustom {

    /**
     * Find showtimes with optional filters using QueryDSL
     *
     * @param movieId optional movie ID filter
     * @param cinemaId optional cinema ID filter
     * @param city optional city filter
     * @param date optional specific date filter (YYYY-MM-DD)
     * @param startDate optional start date range filter (YYYY-MM-DD)
     * @param endDate optional end date range filter (YYYY-MM-DD)
     * @return list of showtimes matching the filters
     */
    List<Showtime> findShowtimesWithFilters(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate);

    /**
     * Find showtimes by movie ID with optional filters
     *
     * @param movieId required movie ID filter
     * @param cinemaId optional cinema ID filter
     * @param city optional city filter
     * @param date optional specific date filter (YYYY-MM-DD)
     * @param startDate optional start date range filter (YYYY-MM-DD)
     * @param endDate optional end date range filter (YYYY-MM-DD)
     * @return list of showtimes matching the filters
     */
    List<Showtime> findShowtimesByMovieId(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate);

    /**
     * Find showtimes by cinema ID with optional filters
     *
     * @param cinemaId required cinema ID filter
     * @param movieId optional movie ID filter
     * @param date optional specific date filter (YYYY-MM-DD)
     * @param startDate optional start date range filter (YYYY-MM-DD)
     * @param endDate optional end date range filter (YYYY-MM-DD)
     * @return list of showtimes matching the filters
     */
    List<Showtime> findShowtimesByCinemaId(
            Long cinemaId, Long movieId, String date, String startDate, String endDate);
}
