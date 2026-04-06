package com.ticket_online.domain.catalog.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.ticket_online.domain.catalog.dao.SeatRepository;
import com.ticket_online.domain.catalog.dao.ShowRepository;
import com.ticket_online.domain.catalog.domain.Show;
import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.security.PrincipalDetails;
import java.time.LocalDateTime;
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
public class SeatServiceTest {
    @Autowired private SeatService seatService;

    @Autowired private UserRepository userRepository;

    @Autowired private SeatRepository seatRepository;

    @Autowired private ShowRepository showRepository;

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
    void createSeatsForShow() {
        // given
        Show show = Show.createShow(LocalDateTime.now(), null, null);
        showRepository.save(show);

        // when
        seatService.createSeatsForShow(show.getId(), 100, 5000L);

        // then
        assertThat(seatRepository.findByShowId(show.getId()).size()).isEqualTo(100);
    }
}
