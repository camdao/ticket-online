package com.ticket_online.domain.booking.dto;

import java.util.List;

public record HoldSeatRequest(Long showId, List<Long> seatIds, Long userId) {}
