package com.ticket_online.domain.movies.dao;

import static com.ticket_online.domain.cinemas.domain.QCinema.cinema;
import static com.ticket_online.domain.movies.domain.QMovie.movie;
import static com.ticket_online.domain.rooms.domain.QRoom.room;
import static com.ticket_online.domain.showtimes.domain.QShowtime.showtime;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.movies.domain.MovieStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryCustomImpl implements MovieRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Movie> findMovies(MovieStatus status, Long cinemaId, Sort sort) {

        BooleanBuilder predicate = new BooleanBuilder();

        if (cinemaId != null) {
            predicate.and(showtime.cinema.id.eq(cinemaId));
        }

        if (status == MovieStatus.NOW_SHOWING) {
            predicate.and(movie.releaseDate.loe(LocalDate.now()));
        }

        if (status == MovieStatus.UPCOMING) {
            predicate.and(movie.releaseDate.gt(LocalDate.now()));
        }

        return queryFactory
                .selectFrom(movie)
                .join(showtime)
                .on(showtime.movie.eq(movie))
                .join(showtime.room, room)
                .join(room.cinema, cinema)
                .where(predicate)
                .distinct()
                .fetch();
    }
}
