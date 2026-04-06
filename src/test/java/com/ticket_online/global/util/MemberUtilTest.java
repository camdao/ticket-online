package com.ticket_online.global.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.security.PrincipalDetails;
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
public class MemberUtilTest {
    @Autowired private UserUtil userUtil;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        PrincipalDetails principal = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User saveUser = userRepository.save(User.createUser("test", "test"));
    }

    @Test
    void shouldReturnCurrentLoggedInUserInfo() {
        // given

        // when
        User currentUser = userUtil.getCurrentUser();
        // then
        assertEquals(1L, currentUser.getId());
    }
}
