package com.ticket_online.domain.user.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
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

    private String email;

    private String name;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder(access = AccessLevel.PRIVATE)
    User(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public static User createUser(String name, String email) {
        return User.builder().name(name).email(email).build();
    }
}
