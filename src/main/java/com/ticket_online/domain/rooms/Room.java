package com.ticket_online.domain.rooms;

import com.ticket_online.domain.cinemas.domain.Cinema;
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
public class Room extends BaseTimeEntity {
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
    Room(Long cinemaId, String name, Integer capacity, String roomType) {
        this.cinemaId = cinemaId;
        this.name = name;
        this.capacity = capacity;
        this.roomType = roomType;
    }

    public static Room createRoom(Long cinemaId, String name, Integer capacity, String roomType) {
        return Room.builder()
                .cinemaId(cinemaId)
                .name(name)
                .capacity(capacity)
                .roomType(roomType)
                .build();
    }

    public void updateRoom(String name, Integer capacity, String roomType) {
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

    public String getType() {
        return this.roomType;
    }

    public Integer getTotalSeats() {
        return this.capacity;
    }
}
