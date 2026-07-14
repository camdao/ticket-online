package com.ticket_online.domain.movies.dao;

import com.ticket_online.domain.movies.domain.Movie;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Find movies by title (case-insensitive partial match)
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Find movies by genre (partial match for multiple genres)
    Page<Movie> findByGenreContainingIgnoreCase(String genre, Pageable pageable);

    // Find movies by rating
    Page<Movie> findByRating(String rating, Pageable pageable);

    // Find movies by director
    Page<Movie> findByDirectorContainingIgnoreCase(String director, Pageable pageable);

    // Find movies released between dates
    Page<Movie> findByReleaseDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Find upcoming movies (release date after today)
    @Query("SELECT m FROM Movie m WHERE m.releaseDate > :today ORDER BY m.releaseDate ASC")
    Page<Movie> findUpcomingMovies(@Param("today") LocalDate today, Pageable pageable);

    // Find now showing movies (release date on or before today)
    @Query("SELECT m FROM Movie m WHERE m.releaseDate <= :today ORDER BY m.releaseDate DESC")
    Page<Movie> findNowShowingMovies(@Param("today") LocalDate today, Pageable pageable);

    // Search movies by multiple criteria
    @Query(
            "SELECT m FROM Movie m WHERE "
                    + "(:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')))"
                    + " AND (:genre IS NULL OR LOWER(m.genre) LIKE LOWER(CONCAT('%', :genre,"
                    + " '%')))"
                    + " AND (:rating IS NULL OR m.rating = :rating)"
                    + " AND (:director IS NULL OR LOWER(m.director) LIKE LOWER(CONCAT('%',"
                    + " :director, '%')))")
    Page<Movie> searchMovies(
            @Param("title") String title,
            @Param("genre") String genre,
            @Param("rating") String rating,
            @Param("director") String director,
            Pageable pageable);

    // Find movies by IDs
    List<Movie> findByIdIn(List<Long> ids);

    // Find movies by status (calculated based on releaseDate)
    @Query(
            "SELECT m FROM Movie m WHERE "
                    + "(:status = 'NOW_SHOWING' AND m.releaseDate <= :today) OR "
                    + "(:status = 'UPCOMING' AND m.releaseDate > :today)")
    Page<Movie> findByStatus(
            @Param("status") String status, @Param("today") LocalDate today, Pageable pageable);
}
