package com.ticket_online.domain.catalog.application;

import com.ticket_online.DatabaseCleaner;
import com.ticket_online.domain.catalog.dao.ShowRepository;
import com.ticket_online.domain.catalog.domain.Show;
import com.ticket_online.domain.catalog.dto.request.CreateShowRequest;
import com.ticket_online.domain.catalog.dto.response.CreateShowResponse;
import com.ticket_online.domain.catalog.dto.response.FindShowResponse;
import com.ticket_online.domain.catalog.dto.response.SeatResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ShowServiceTest {
    @Autowired private ShowService showService;
    @Autowired private ShowRepository showRepository;
    @Autowired ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        DatabaseCleaner.clear(applicationContext);
    }

    @Test
    void findAllShow() {
        // given
        Show show1 = Show.createShow(null, "Show 1", "Location 1");
        Show show2 = Show.createShow(null, "Show 2", "Location 2");

        showRepository.save(show1);
        showRepository.save(show2);
        // when
        List<FindShowResponse> shows = showService.findAllShow();
        // then
        assert shows.size() >= 2;
    }

    @Test
    void createShow() {
        // given
        String name = "New Show";
        String location = "New Location";
        int totalSeats = 100;

        // when
        showService.createShow(
                new CreateShowRequest(
                        name,
                        LocalDateTime.now(),
                        location,
                        totalSeats,
                        BigDecimal.valueOf(totalSeats).longValue()));

        // then
        List<FindShowResponse> shows = showService.findAllShow();
        assert shows.stream()
                .anyMatch(show -> show.name().equals(name) && show.location().equals(location));
    }

    @Test
    void findSeatsByShow() {
        // given
        String name = "New Show";
        String location = "New Location";
        int totalSeats = 100;

        CreateShowResponse show =
                showService.createShow(
                        new CreateShowRequest(
                                name,
                                LocalDateTime.now(),
                                location,
                                totalSeats,
                                BigDecimal.valueOf(totalSeats).longValue()));

        // when
        List<SeatResponse> seats = showService.findSeatsByShow(show.id());

        // then
        assert seats.size() == totalSeats;
    }
}
