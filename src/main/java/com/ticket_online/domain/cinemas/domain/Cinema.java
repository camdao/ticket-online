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
@Table(name = "cinemas")
public class Cinema extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder(access = AccessLevel.PRIVATE)
    Cinema(
            String name,
            String brand,
            String logoUrl,
            String address,
            String district,
            String city,
            String phone,
            String website,
            String description) {
        this.name = name;
        this.brand = brand;
        this.logoUrl = logoUrl;
        this.address = address;
        this.district = district;
        this.city = city;
        this.phone = phone;
        this.website = website;
        this.description = description;
    }

    public static Cinema createCinema(
            String name,
            String brand,
            String logoUrl,
            String address,
            String district,
            String city,
            String phone,
            String website,
            String description) {
        return Cinema.builder()
                .name(name)
                .brand(brand)
                .logoUrl(logoUrl)
                .address(address)
                .district(district)
                .city(city)
                .phone(phone)
                .website(website)
                .description(description)
                .build();
    }

    public void updateCinema(
            String name,
            String brand,
            String logoUrl,
            String address,
            String district,
            String city,
            String phone,
            String website,
            String description) {
        this.name = name;
        this.brand = brand;
        this.logoUrl = logoUrl;
        this.address = address;
        this.district = district;
        this.city = city;
        this.phone = phone;
        this.website = website;
        this.description = description;
    }

    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        if (address != null) fullAddress.append(address);
        if (district != null) fullAddress.append(", ").append(district);
        if (city != null) fullAddress.append(", ").append(city);
        return fullAddress.toString();
    }
}
