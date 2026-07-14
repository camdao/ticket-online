package com.ticket_online.domain.cinemas.domain;

import com.ticket_online.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "rooms")
public class Screen extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "cinema_id", nullable = false)
    private Long cinemaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", insertable = false, updatable = false)
    private Cinema cinema;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "room_type", length = 50)
    private String roomType;

    @Builder(access = AccessLevel.PRIVATE)
    Screen(Long cinemaId, String name, Integer capacity, String roomType) {
        this.cinemaId = cinemaId;
        this.name = name;
        this.capacity = capacity;
        this.roomType = roomType;
    }

    public static Screen createScreen(
            Long cinemaId, String name, Integer capacity, String roomType) {
        return Screen.builder()
                .cinemaId(cinemaId)
                .name(name)
                .capacity(capacity)
                .roomType(roomType)
                .build();
    }

    public void updateScreen(String name, Integer capacity, String roomType) {
        this.name = name;
        this.capacity = capacity;
        this.roomType = roomType;
    }

    public boolean isStandardRoom() {
        return "Standard".equalsIgnoreCase(roomType);
    }

    public boolean isPremiumRoom() {
        return roomType != null
                && (roomType.equalsIgnoreCase("IMAX")
                        || roomType.equalsIgnoreCase("VIP")
                        || roomType.equalsIgnoreCase("4DX"));
    }
}
