package com.ticket_online.domain.catalog.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import com.ticket_online.domain.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Show extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime start_time;

    private String location;

    @Builder(access = AccessLevel.PRIVATE)
    Show(LocalDateTime start_time, String name,String location) {
        this.name = name;
        this.start_time = start_time;
        this.location = location;
    }

    public static Show createShow(LocalDateTime start_time, String name,String location) {
        return Show.builder()
                .name(name)
                .start_time(start_time)
                .location(location).build();
    }
}
