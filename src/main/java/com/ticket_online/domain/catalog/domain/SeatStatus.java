package com.ticket_online.domain.catalog.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeatStatus {
    AVAILABLE("Available"),
    SOLD("Sold");

    private final String value;
}
