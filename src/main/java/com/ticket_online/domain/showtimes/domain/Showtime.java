package com.ticket_online.domain.showtimes.domain;

import com.ticket_online.domain.cinemas.domain.Cinema;
import com.ticket_online.domain.model.BaseTimeEntity;
import com.ticket_online.domain.movies.domain.Movie;
import com.ticket_online.domain.rooms.Room;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "showtimes")
public class Showtime extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShowtimeStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    Showtime(
            Movie movie,
            Room room,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal basePrice,
            ShowtimeStatus status) {
        this.movie = movie;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.status = status;
    }

    public static Showtime createShowtime(
            Movie movie,
            Room room,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal basePrice) {
        return Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(startTime)
                .endTime(endTime)
                .basePrice(basePrice)
                .status(ShowtimeStatus.ACTIVE)
                .build();
    }

    public Cinema getCinema() {
        return this.room.getCinema();
    }

    public void updateShowtime(
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal basePrice,
            ShowtimeStatus status) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.status = status;
    }

    public void cancel() {
        this.status = ShowtimeStatus.CANCELLED;
    }

    public boolean isActive() {
        return this.status == ShowtimeStatus.ACTIVE;
    }

    public boolean isPast() {
        return this.endTime.isBefore(LocalDateTime.now());
    }

    public boolean isUpcoming() {
        return this.startTime.isAfter(LocalDateTime.now());
    }

    public void updateStatus(ShowtimeStatus showtimeStatus) {
        this.status = showtimeStatus;
    }
}
