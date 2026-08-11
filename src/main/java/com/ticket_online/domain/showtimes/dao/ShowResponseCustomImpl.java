package com.ticket_online.domain.showtimes.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket_online.domain.cinemas.domain.QCinema;
import com.ticket_online.domain.movies.domain.QMovie;
import com.ticket_online.domain.rooms.domain.QRoom;
import com.ticket_online.domain.showtimes.domain.QShowtime;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.showtimes.domain.ShowtimeStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShowResponseCustomImpl implements ShowResponseCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Showtime> findShowtimesWithFilters(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate) {

        QShowtime showtime = QShowtime.showtime;
        QMovie movie = QMovie.movie;
        QRoom room = QRoom.room;
        QCinema cinema = QCinema.cinema;

        BooleanBuilder builder = new BooleanBuilder();

        // Filter by movie (optional)
        if (movieId != null) {
            builder.and(showtime.movie.id.eq(movieId));
        }

        // Filter by cinema (optional)
        if (cinemaId != null) {
            builder.and(showtime.room.cinema.id.eq(cinemaId));
        }

        // Filter by city (optional)
        if (city != null && !city.isBlank()) {
            builder.and(showtime.room.cinema.city.eq(city));
        }

        // Filter by date
        if (date != null && !date.isBlank()) {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);
            builder.and(showtime.startTime.between(startOfDay, endOfDay));
        } else if (startDate != null && !startDate.isBlank()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            builder.and(showtime.startTime.goe(start));

            if (endDate != null && !endDate.isBlank()) {
                LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                builder.and(showtime.startTime.loe(end));
            }
        }

        // Only active showtimes
        builder.and(showtime.status.eq(ShowtimeStatus.ACTIVE));

        // Future showtimes only
        builder.and(showtime.startTime.goe(LocalDateTime.now()));

        return jpaQueryFactory
                .selectFrom(showtime)
                .join(showtime.movie, movie)
                .fetchJoin()
                .join(showtime.room, room)
                .fetchJoin()
                .join(room.cinema, cinema)
                .fetchJoin()
                .where(builder)
                .fetch();
    }

    @Override
    public List<Showtime> findShowtimesByMovieId(
            Long movieId,
            Long cinemaId,
            String city,
            String date,
            String startDate,
            String endDate) {

        QShowtime showtime = QShowtime.showtime;
        QMovie movie = QMovie.movie;
        QRoom room = QRoom.room;
        QCinema cinema = QCinema.cinema;

        BooleanBuilder builder = new BooleanBuilder();

        // Filter by movie (required)
        builder.and(showtime.movie.id.eq(movieId));

        // Filter by cinema (optional)
        if (cinemaId != null) {
            builder.and(showtime.room.cinema.id.eq(cinemaId));
        }

        // Filter by city (optional)
        if (city != null && !city.isBlank()) {
            builder.and(showtime.room.cinema.city.eq(city));
        }

        // Filter by date
        if (date != null && !date.isBlank()) {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);
            builder.and(showtime.startTime.between(startOfDay, endOfDay));
        } else if (startDate != null && !startDate.isBlank()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            builder.and(showtime.startTime.goe(start));

            if (endDate != null && !endDate.isBlank()) {
                LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                builder.and(showtime.startTime.loe(end));
            }
        }

        // Only active showtimes
        builder.and(showtime.status.eq(ShowtimeStatus.ACTIVE));

        // Future showtimes only
        builder.and(showtime.startTime.goe(LocalDateTime.now()));

        return jpaQueryFactory
                .selectFrom(showtime)
                .join(showtime.movie, movie)
                .fetchJoin()
                .join(showtime.room, room)
                .fetchJoin()
                .join(room.cinema, cinema)
                .fetchJoin()
                .where(builder)
                .fetch();
    }

    @Override
    public List<Showtime> findShowtimesByCinemaId(
            Long cinemaId, Long movieId, String date, String startDate, String endDate) {

        QShowtime showtime = QShowtime.showtime;
        QMovie movie = QMovie.movie;
        QRoom room = QRoom.room;
        QCinema cinema = QCinema.cinema;

        BooleanBuilder builder = new BooleanBuilder();

        // Filter by cinema (required)
        builder.and(showtime.room.cinema.id.eq(cinemaId));

        // Filter by movie (optional)
        if (movieId != null) {
            builder.and(showtime.movie.id.eq(movieId));
        }

        // Filter by date
        if (date != null && !date.isBlank()) {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);
            builder.and(showtime.startTime.between(startOfDay, endOfDay));
        } else if (startDate != null && !startDate.isBlank()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            builder.and(showtime.startTime.goe(start));

            if (endDate != null && !endDate.isBlank()) {
                LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                builder.and(showtime.startTime.loe(end));
            }
        }

        // Only active showtimes
        builder.and(showtime.status.eq(ShowtimeStatus.ACTIVE));

        // Future showtimes only
        builder.and(showtime.startTime.goe(LocalDateTime.now()));

        return jpaQueryFactory
                .selectFrom(showtime)
                .join(showtime.movie, movie)
                .fetchJoin()
                .join(showtime.room, room)
                .fetchJoin()
                .join(room.cinema, cinema)
                .fetchJoin()
                .where(builder)
                .fetch();
    }

    @Override
    public List<LocalDate> findDistinctShowtimeDates(Long movieId, Long cinemaId) {
        QShowtime showtime = QShowtime.showtime;

        BooleanBuilder builder = new BooleanBuilder();

        // Filter by movie (required)
        builder.and(showtime.movie.id.eq(movieId));

        // Filter by cinema (required)
        builder.and(showtime.room.cinema.id.eq(cinemaId));

        // Only active showtimes
        builder.and(showtime.status.eq(ShowtimeStatus.ACTIVE));

        // Future showtimes only
        builder.and(showtime.startTime.goe(LocalDateTime.now()));

        // Extract date from startTime and get distinct dates
        return jpaQueryFactory
                .select(Expressions.dateTemplate(LocalDate.class, "DATE({0})", showtime.startTime))
                .from(showtime)
                .where(builder)
                .distinct()
                .orderBy(
                        Expressions.dateTemplate(LocalDate.class, "DATE({0})", showtime.startTime)
                                .asc())
                .fetch();
    }
}
