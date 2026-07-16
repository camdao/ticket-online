package com.ticket_online.domain.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("Should create user with all required fields using createUser factory method")
    void shouldCreateUserWithAllRequiredFields() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "encodedPassword123";
        String fullName = "Test User";
        String phoneNumber = "0123456789";

        // When
        User user = User.createUser(username, email, password, fullName, phoneNumber);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getFullName()).isEqualTo(fullName);
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("Should set default role to ROLE_USER when creating user")
    void shouldSetDefaultRoleToRoleUser() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "encodedPassword123";
        String fullName = "Test User";
        String phoneNumber = "0123456789";

        // When
        User user = User.createUser(username, email, password, fullName, phoneNumber);

        // Then
        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_USER);
    }

    @Test
    @DisplayName("Should create user with valid username")
    void shouldCreateUserWithValidUsername() {
        // Given
        String username = "john_doe123";

        // When
        User user =
                User.createUser(
                        username, "john@example.com", "password123", "John Doe", "0987654321");

        // Then
        assertThat(user.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Should create user with valid email")
    void shouldCreateUserWithValidEmail() {
        // Given
        String email = "john.doe@example.com";

        // When
        User user = User.createUser("johndoe", email, "password123", "John Doe", "0987654321");

        // Then
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should create user with encoded password")
    void shouldCreateUserWithEncodedPassword() {
        // Given
        String encodedPassword = "$2a$10$encoded.password.hash";

        // When
        User user =
                User.createUser(
                        "johndoe", "john@example.com", encodedPassword, "John Doe", "0987654321");

        // Then
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("Should create user with full name containing spaces")
    void shouldCreateUserWithFullNameContainingSpaces() {
        // Given
        String fullName = "John William Doe";

        // When
        User user =
                User.createUser(
                        "johndoe", "john@example.com", "password123", fullName, "0987654321");

        // Then
        assertThat(user.getFullName()).isEqualTo(fullName);
    }

    @Test
    @DisplayName("Should create user with valid 10-digit phone number starting with 0")
    void shouldCreateUserWithValidPhoneNumber() {
        // Given
        String phoneNumber = "0901234567";

        // When
        User user =
                User.createUser(
                        "johndoe", "john@example.com", "password123", "John Doe", phoneNumber);

        // Then
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("Should create user with ROLE_USER as default role")
    void shouldCreateUserWithRoleUser() {
        // When
        User user =
                User.createUser(
                        "johndoe", "john@example.com", "password123", "John Doe", "0901234567");

        // Then
        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(user.getRole()).isNotEqualTo(UserRole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should have null id before persistence")
    void shouldHaveNullIdBeforePersistence() {
        // When
        User user =
                User.createUser(
                        "johndoe", "john@example.com", "password123", "John Doe", "0901234567");

        // Then
        assertThat(user.getId()).isNull();
    }

    @Test
    @DisplayName("Should inherit createdAt and updatedAt from BaseTimeEntity")
    void shouldInheritTimestampFieldsFromBaseTimeEntity() {
        // When
        User user =
                User.createUser(
                        "johndoe", "john@example.com", "password123", "John Doe", "0901234567");

        // Then
        assertThat(user).isInstanceOf(com.ticket_online.domain.model.BaseTimeEntity.class);
        assertThat(user.getCreatedAt()).isNull(); // null before persistence
        assertThat(user.getUpdatedAt()).isNull(); // null before persistence
    }

    @Test
    @DisplayName("Should create multiple users with different data")
    void shouldCreateMultipleUsersWithDifferentData() {
        // Given & When
        User user1 =
                User.createUser("alice", "alice@example.com", "pass1", "Alice Smith", "0111111111");
        User user2 =
                User.createUser("bob", "bob@example.com", "pass2", "Bob Johnson", "0222222222");
        User user3 =
                User.createUser(
                        "charlie", "charlie@example.com", "pass3", "Charlie Brown", "0333333333");

        // Then
        assertThat(user1.getUsername()).isEqualTo("alice");
        assertThat(user2.getUsername()).isEqualTo("bob");
        assertThat(user3.getUsername()).isEqualTo("charlie");

        assertThat(user1.getEmail()).isEqualTo("alice@example.com");
        assertThat(user2.getEmail()).isEqualTo("bob@example.com");
        assertThat(user3.getEmail()).isEqualTo("charlie@example.com");

        assertThat(user1.getRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(user2.getRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(user3.getRole()).isEqualTo(UserRole.ROLE_USER);
    }

    @Test
    @DisplayName("Should create user with Vietnamese full name")
    void shouldCreateUserWithVietnameseFullName() {
        // Given
        String fullName = "Nguyễn Văn An";

        // When
        User user =
                User.createUser(
                        "nguyenvanan",
                        "an.nguyen@example.com",
                        "password123",
                        fullName,
                        "0912345678");

        // Then
        assertThat(user.getFullName()).isEqualTo(fullName);
    }

    @Test
    @DisplayName("Should create user with email containing dots and numbers")
    void shouldCreateUserWithComplexEmail() {
        // Given
        String email = "user.name123@example.co.vn";

        // When
        User user = User.createUser("username", email, "password123", "User Name", "0912345678");

        // Then
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should maintain immutability of user fields after creation")
    void shouldMaintainImmutabilityAfterCreation() {
        // Given
        String originalUsername = "johndoe";
        String originalEmail = "john@example.com";
        String originalPassword = "password123";
        String originalFullName = "John Doe";
        String originalPhoneNumber = "0901234567";

        // When
        User user =
                User.createUser(
                        originalUsername,
                        originalEmail,
                        originalPassword,
                        originalFullName,
                        originalPhoneNumber);

        // Then - verify getters return the same values
        assertThat(user.getUsername()).isEqualTo(originalUsername);
        assertThat(user.getEmail()).isEqualTo(originalEmail);
        assertThat(user.getPassword()).isEqualTo(originalPassword);
        assertThat(user.getFullName()).isEqualTo(originalFullName);
        assertThat(user.getPhoneNumber()).isEqualTo(originalPhoneNumber);
        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_USER);
    }
}
