package com.ticket_online.domain.booking.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ticket_online.domain.booking.dto.request.HoldSeatRequest;
import com.ticket_online.domain.catalog.dao.SeatRepository;
import com.ticket_online.domain.catalog.dao.ShowRepository;
import com.ticket_online.domain.catalog.domain.Seat;
import com.ticket_online.domain.catalog.domain.Show;
import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.security.PrincipalDetails;
import com.ticket_online.global.util.RedisSeatScripts;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SeatHoldServiceTest {
    @Autowired private UserRepository userRepository;

    @Autowired private ShowRepository showRepository;

    @Autowired private SeatRepository seatRepository;

    @Autowired private RedisSeatScripts redisSeatScripts;

    @BeforeEach
    void setUp() {
        PrincipalDetails principal = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, "password", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User saveUser = userRepository.save(User.createUser("test", "test"));
    }

    @Test
    void holdSeats() {
        // given
        HoldSeatRequest request = new HoldSeatRequest(1L, List.of(1L, 2L));
        showRepository.save(Show.createShow(LocalDateTime.now(), null, null));
        Show show = showRepository.findById(1L).orElseThrow();
        seatRepository.saveAll(
                List.of(
                        Seat.createSeat(show, "A1", BigDecimal.valueOf(10L)),
                        Seat.createSeat(show, "A2", BigDecimal.valueOf(10L))));
        // when
        redisSeatScripts.holdSeats(request.seatIds(), request.showId(), 1L, 1800);
        // then
        assertNotNull(
                redisSeatScripts.checkAndExtendSeats(
                        request.showId(), request.seatIds(), 1L, 1800));
    }
}
