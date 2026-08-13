package com.ticket_online.global.util;

import com.ticket_online.domain.user.domain.UserRole;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * Test utility for managing SecurityContext in tests. Provides helper methods for setting up
 * authenticated users in the Spring Security context.
 */
public class SecurityContextTestUtil {

    /**
     * Sets up an authenticated user in the SecurityContext.
     *
     * @param userId the user ID to authenticate
     * @param role the user's role
     */
    public static void setAuthentication(Long userId, UserRole role) {
        Authentication authentication = createAuthentication(userId, role);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Sets up an authenticated user with default ROLE_USER.
     *
     * @param userId the user ID to authenticate
     */
    public static void setAuthentication(Long userId) {
        setAuthentication(userId, UserRole.ROLE_USER);
    }

    /** Clears the current SecurityContext authentication. */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Creates a mock Authentication object for testing.
     *
     * @param userId the user ID
     * @param role the user's role
     * @return an Authentication object with the specified user ID and role
     */
    public static Authentication createAuthentication(Long userId, UserRole role) {
        return new UsernamePasswordAuthenticationToken(
                userId.toString(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role.name())));
    }

    /**
     * Creates a mock Authentication object with default ROLE_USER.
     *
     * @param userId the user ID
     * @return an Authentication object with the specified user ID and ROLE_USER
     */
    public static Authentication createAuthentication(Long userId) {
        return createAuthentication(userId, UserRole.ROLE_USER);
    }

    /**
     * Creates a SecurityContext with an authenticated user. Useful with @WithSecurityContext
     * annotations.
     *
     * @param userId the user ID to authenticate
     * @return a SecurityContext with the authenticated user
     */
    public static SecurityContext createSecurityContext(Long userId) {
        return createSecurityContext(userId, UserRole.ROLE_USER);
    }

    /**
     * Creates a SecurityContext with an authenticated user and specific role.
     *
     * @param userId the user ID to authenticate
     * @param role the user's role
     * @return a SecurityContext with the authenticated user
     */
    public static SecurityContext createSecurityContext(Long userId, UserRole role) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(createAuthentication(userId, role));
        return context;
    }

    /**
     * Custom annotation for setting up authenticated user in tests. Use as:
     *
     * <pre>
     * {@code @Test}
     * {@code @WithMockAuthenticatedUser(userId = 1L, role = UserRole.ROLE_ADMIN)}
     * void testAdminOnlyEndpoint() {
     *     // Test code here
     * }
     * </pre>
     */
    public @interface WithMockAuthenticatedUser {
        long userId() default 1L;

        UserRole role() default UserRole.ROLE_USER;
    }

    /** Factory for creating SecurityContext from @WithMockAuthenticatedUser annotation. */
    public static class WithMockAuthenticatedUserSecurityContextFactory
            implements WithSecurityContextFactory<WithMockAuthenticatedUser> {

        @Override
        public SecurityContext createSecurityContext(WithMockAuthenticatedUser annotation) {
            return SecurityContextTestUtil.createSecurityContext(
                    annotation.userId(), annotation.role());
        }
    }
}
