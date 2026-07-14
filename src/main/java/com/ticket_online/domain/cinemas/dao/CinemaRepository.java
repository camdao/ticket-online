package com.ticket_online.domain.cinemas.dao;

import com.ticket_online.domain.cinemas.domain.Cinema;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    List<Cinema> findByBrand(String brand);

    List<Cinema> findByCity(String city);

    List<Cinema> findByDistrict(String district);

    List<Cinema> findByCityAndDistrict(String city, String district);

    @Query(
            "SELECT c FROM Cinema c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR LOWER(c.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Cinema> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT c.brand FROM Cinema c ORDER BY c.brand")
    List<String> findAllBrands();

    @Query("SELECT DISTINCT c.city FROM Cinema c ORDER BY c.city")
    List<String> findAllCities();
}
