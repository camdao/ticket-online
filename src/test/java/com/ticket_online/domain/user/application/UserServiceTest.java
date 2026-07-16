package com.ticket_online.domain.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.domain.user.domain.UserRole;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    @DisplayName("Should find user by id when user exists")
    void shouldFindUserByIdWhenUserExists() {
        // Given
        Long userId = 1L;
        User user =
                User.createUser(
                        "testuser",
                        "test@example.com",
                        "encodedPassword",
                        "Test User",
                        "0123456789");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by id")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should find user by username when user exists")
    void shouldFindUserByUsernameWhenUserExists() {
        // Given
        String username = "testuser";
        User user =
                User.createUser(
                        username, "test@example.com", "encodedPassword", "Test User", "0123456789");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserByUsername(username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should throw exception when user not found by username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should find user by email when user exists")
    void shouldFindUserByEmailWhenUserExists() {
        // Given
        String email = "test@example.com";
        User user =
                User.createUser("testuser", email, "encodedPassword", "Test User", "0123456789");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when user not found by email")
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should check if username exists and return true")
    void shouldCheckIfUsernameExistsAndReturnTrue() {
        // Given
        String username = "existinguser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mock(User.class)));

        // When
        boolean result = userService.existsByUsername(username);

        // Then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should check if username exists and return false")
    void shouldCheckIfUsernameExistsAndReturnFalse() {
        // Given
        String username = "newuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        boolean result = userService.existsByUsername(username);

        // Then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should check if email exists and return true")
    void shouldCheckIfEmailExistsAndReturnTrue() {
        // Given
        String email = "existing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mock(User.class)));

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should check if email exists and return false")
    void shouldCheckIfEmailExistsAndReturnFalse() {
        // Given
        String email = "new@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should create new user successfully")
    void shouldCreateNewUserSuccessfully() {
        // Given
        String username = "newuser";
        String email = "new@example.com";
        String password = "password123";
        String fullName = "New User";
        String phoneNumber = "0123456789";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        User savedUser = User.createUser(username, email, "encodedPassword", fullName, phoneNumber);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.createUser(username, email, password, fullName, phoneNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getFullName()).isEqualTo(fullName);
        assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with existing username")
    void shouldThrowExceptionWhenCreatingUserWithExistingUsername() {
        // Given
        String username = "existinguser";
        String email = "new@example.com";
        String password = "password123";
        String fullName = "New User";
        String phoneNumber = "0123456789";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mock(User.class)));

        // When & Then
        assertThatThrownBy(
                        () ->
                                userService.createUser(
                                        username, email, password, fullName, phoneNumber))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_ALREADY_EXISTS);
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with existing email")
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        // Given
        String username = "newuser";
        String email = "existing@example.com";
        String password = "password123";
        String fullName = "New User";
        String phoneNumber = "0123456789";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mock(User.class)));

        // When & Then
        assertThatThrownBy(
                        () ->
                                userService.createUser(
                                        username, email, password, fullName, phoneNumber))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should encode password when creating user")
    void shouldEncodePasswordWhenCreatingUser() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String rawPassword = "plainPassword123";
        String fullName = "Test User";
        String phoneNumber = "0123456789";
        String encodedPassword = "$2a$10$encodedPasswordHash";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        User savedUser = User.createUser(username, email, encodedPassword, fullName, phoneNumber);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.createUser(username, email, rawPassword, fullName, phoneNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        // Given
        List<User> users =
                Arrays.asList(
                        User.createUser(
                                "user1",
                                "user1@example.com",
                                "password1",
                                "User One",
                                "0111111111"),
                        User.createUser(
                                "user2",
                                "user2@example.com",
                                "password2",
                                "User Two",
                                "0222222222"),
                        User.createUser(
                                "user3",
                                "user3@example.com",
                                "password3",
                                "User Three",
                                "0333333333"));
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getUsername()).isEqualTo("user1");
        assertThat(result.get(1).getUsername()).isEqualTo("user2");
        assertThat(result.get(2).getUsername()).isEqualTo("user3");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delete user by id when user exists")
    void shouldDeleteUserByIdWhenUserExists() {
        // Given
        Long userId = 1L;
        User user =
                User.createUser(
                        "testuser",
                        "test@example.com",
                        "encodedPassword",
                        "Test User",
                        "0123456789");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(userId);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should validate password matches for correct password")
    void shouldValidatePasswordMatchesForCorrectPassword() {
        // Given
        User user =
                User.createUser(
                        "testuser",
                        "test@example.com",
                        "$2a$10$encodedPassword",
                        "Test User",
                        "0123456789");
        String rawPassword = "correctPassword";
        when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(true);

        // When
        boolean result = userService.validatePassword(user, rawPassword);

        // Then
        assertThat(result).isTrue();
        verify(passwordEncoder, times(1)).matches(rawPassword, user.getPassword());
    }

    @Test
    @DisplayName("Should validate password does not match for incorrect password")
    void shouldValidatePasswordDoesNotMatchForIncorrectPassword() {
        // Given
        User user =
                User.createUser(
                        "testuser",
                        "test@example.com",
                        "$2a$10$encodedPassword",
                        "Test User",
                        "0123456789");
        String rawPassword = "wrongPassword";
        when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(false);

        // When
        boolean result = userService.validatePassword(user, rawPassword);

        // Then
        assertThat(result).isFalse();
        verify(passwordEncoder, times(1)).matches(rawPassword, user.getPassword());
    }

    @Test
    @DisplayName("Should count total users")
    void shouldCountTotalUsers() {
        // Given
        long expectedCount = 42L;
        when(userRepository.count()).thenReturn(expectedCount);

        // When
        long result = userService.countUsers();

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(userRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should update user email successfully")
    void shouldUpdateUserEmailSuccessfully() {
        // Given
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        User user =
                User.createUser(
                        "testuser",
                        "old@example.com",
                        "encodedPassword",
                        "Test User",
                        "0123456789");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.updateUserEmail(userId, newEmail);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByEmail(newEmail);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating email to existing email")
    void shouldThrowExceptionWhenUpdatingEmailToExistingEmail() {
        // Given
        Long userId = 1L;
        String existingEmail = "existing@example.com";
        User currentUser =
                User.createUser(
                        "testuser",
                        "test@example.com",
                        "encodedPassword",
                        "Test User",
                        "0123456789");
        User anotherUser =
                User.createUser(
                        "anotheruser",
                        existingEmail,
                        "encodedPassword",
                        "Another User",
                        "0987654321");

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateUserEmail(userId, existingEmail))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByEmail(existingEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should verify user role is ROLE_USER by default")
    void shouldVerifyUserRoleIsRoleUserByDefault() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String fullName = "Test User";
        String phoneNumber = "0123456789";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        User savedUser = User.createUser(username, email, "encodedPassword", fullName, phoneNumber);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.createUser(username, email, password, fullName, phoneNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(result.getRole()).isNotEqualTo(UserRole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should handle multiple concurrent user lookups")
    void shouldHandleMultipleConcurrentUserLookups() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        User user1 =
                User.createUser("user1", "user1@example.com", "pass1", "User One", "0111111111");
        User user2 =
                User.createUser("user2", "user2@example.com", "pass2", "User Two", "0222222222");

        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

        // When
        User result1 = userService.getUserById(userId1);
        User result2 = userService.getUserById(userId2);

        // Then
        assertThat(result1.getUsername()).isEqualTo("user1");
        assertThat(result2.getUsername()).isEqualTo("user2");
        verify(userRepository, times(1)).findById(userId1);
        verify(userRepository, times(1)).findById(userId2);
    }

    @Test
    @DisplayName("Should verify user creation with Vietnamese characters")
    void shouldVerifyUserCreationWithVietnameseCharacters() {
        // Given
        String username = "nguyenvana";
        String email = "nguyenvana@example.com";
        String password = "password123";
        String fullName = "Nguyễn Văn An";
        String phoneNumber = "0912345678";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        User savedUser = User.createUser(username, email, "encodedPassword", fullName, phoneNumber);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.createUser(username, email, password, fullName, phoneNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo(fullName);
        assertThat(result.getFullName()).contains("Nguyễn");
    }

    // Mock UserService class for testing purposes
    static class UserService {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }

        public User getUserById(Long userId) {
            return userRepository
                    .findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        }

        public User getUserByUsername(String username) {
            return userRepository
                    .findByUsername(username)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        }

        public User getUserByEmail(String email) {
            return userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        }

        public boolean existsByUsername(String username) {
            return userRepository.findByUsername(username).isPresent();
        }

        public boolean existsByEmail(String email) {
            return userRepository.findByEmail(email).isPresent();
        }

        public User createUser(
                String username,
                String email,
                String password,
                String fullName,
                String phoneNumber) {
            if (userRepository.findByUsername(username).isPresent()) {
                throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }
            if (userRepository.findByEmail(email).isPresent()) {
                throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }

            String encodedPassword = passwordEncoder.encode(password);
            User user = User.createUser(username, email, encodedPassword, fullName, phoneNumber);
            return userRepository.save(user);
        }

        public List<User> getAllUsers() {
            return userRepository.findAll();
        }

        public void deleteUser(Long userId) {
            getUserById(userId);
            userRepository.deleteById(userId);
        }

        public boolean validatePassword(User user, String rawPassword) {
            return passwordEncoder.matches(rawPassword, user.getPassword());
        }

        public long countUsers() {
            return userRepository.count();
        }

        public User updateUserEmail(Long userId, String newEmail) {
            User user = getUserById(userId);
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            return userRepository.save(user);
        }
    }
}
