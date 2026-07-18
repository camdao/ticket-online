package com.ticket_online.domain.cinemas.dao;

import com.ticket_online.domain.cinemas.domain.Cinema;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    @Query(
            "SELECT c FROM Cinema c WHERE "
                    + "(:brand IS NULL OR c.brand = :brand) AND "
                    + "(:city IS NULL OR c.city = :city) AND "
                    + "(:district IS NULL OR c.district = :district)")
    Page<Cinema> findByFilters(
            @Param("brand") String brand,
            @Param("city") String city,
            @Param("district") String district,
            Pageable pageable);

    @Query(
            "SELECT c FROM Cinema c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR LOWER(c.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Cinema> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT c.brand FROM Cinema c ORDER BY c.brand")
    List<String> findAllBrands();

    @Query("SELECT DISTINCT c.city FROM Cinema c ORDER BY c.city")
    List<String> findAllCities();
}
