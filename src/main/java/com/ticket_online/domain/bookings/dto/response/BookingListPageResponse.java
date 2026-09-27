package com.ticket_online.domain.bookings.dto.response;

import java.util.List;

public record BookingListPageResponse(
        List<BookingListResponse> content, int page, int size, boolean hasNext) {}
