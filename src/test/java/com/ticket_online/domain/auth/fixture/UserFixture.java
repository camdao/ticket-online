package com.ticket_online.domain.auth.fixture;

import com.ticket_online.domain.user.domain.User;
import com.ticket_online.domain.user.domain.UserRole;
import java.lang.reflect.Field;

/**
 * Test fixture for creating User entities with various configurations. Uses reflection to set IDs
 * for testing without database persistence.
 */
public class UserFixture {

    private static final String DEFAULT_USERNAME = "testuser";
    private static final String DEFAULT_EMAIL = "testuser@example.com";
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String DEFAULT_FULL_NAME = "Test User";
    private static final String DEFAULT_PHONE = "0123456789";

    /** Creates a valid User with default values. */
    public static User createValidUser() {
        return User.createUser(
                DEFAULT_USERNAME,
                DEFAULT_EMAIL,
                DEFAULT_PASSWORD,
                DEFAULT_FULL_NAME,
                DEFAULT_PHONE);
    }

    /** Creates a User with a specific ID using reflection. Pattern from CinemaServiceTest. */
    public static User createUserWithId(Long id) {
        User user = createValidUser();
        setId(user, id);
        return user;
    }

    /** Creates a User with a specific role. */
    public static User createUserWithRole(UserRole role) {
        User user =
                User.createUser(
                        DEFAULT_USERNAME,
                        DEFAULT_EMAIL,
                        DEFAULT_PASSWORD,
                        DEFAULT_FULL_NAME,
                        DEFAULT_PHONE);
        setRole(user, role);
        return user;
    }

    /** Creates a User with specific credentials. */
    public static User createUserWithCredentials(String username, String password) {
        return User.createUser(username, DEFAULT_EMAIL, password, DEFAULT_FULL_NAME, DEFAULT_PHONE);
    }

    /** Creates a User with all custom fields. */
    public static User createUser(
            String username, String email, String password, String fullName, String phoneNumber) {
        return User.createUser(username, email, password, fullName, phoneNumber);
    }

    /** Creates a User with specific ID and role. */
    public static User createUserWithIdAndRole(Long id, UserRole role) {
        User user = createUserWithRole(role);
        setId(user, id);
        return user;
    }

    /** Sets ID using reflection (pattern from CinemaServiceTest). */
    private static void setId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set User ID via reflection", e);
        }
    }

    /** Sets role using reflection. */
    private static void setRole(User user, UserRole role) {
        try {
            Field roleField = User.class.getDeclaredField("role");
            roleField.setAccessible(true);
            roleField.set(user, role);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set User role via reflection", e);
        }
    }
}
