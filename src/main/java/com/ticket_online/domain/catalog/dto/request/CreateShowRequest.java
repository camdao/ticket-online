package com.ticket_online.domain.catalog.dto.request;

import java.time.LocalDateTime;

public record CreateShowRequest(
        String name, LocalDateTime startTime, String location, Long totalSeats) {}
