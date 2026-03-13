package com.ticket_online.global.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.security.PrincipalDetails;
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

    @Test
    void shouldReturnCurrentLoggedInUserInfo() {
        // given
        PrincipalDetails principal = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, "password", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = User.createUser("test", "test");
        User saveUser = userRepository.save(user);
        // when
        User currentUser = userUtil.getCurrentUser();
        // then
        assertEquals(saveUser.getId(), currentUser.getId());
    }
}
