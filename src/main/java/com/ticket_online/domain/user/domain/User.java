package com.ticket_online.domain.user.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(nullable = false)
    private String fullName;

    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must be 10 digits and start with 0")
    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder(access = AccessLevel.PRIVATE)
    private User(
            String username,
            String email,
            String password,
            String fullName,
            String phoneNumber,
            UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role != null ? role : UserRole.ROLE_USER;
    }

    public static User createUser(
            String username, String email, String password, String fullName, String phoneNumber) {
        return User.builder()
                .username(username)
                .email(email)
                .password(password)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .role(UserRole.ROLE_USER)
                .build();
    }
}
