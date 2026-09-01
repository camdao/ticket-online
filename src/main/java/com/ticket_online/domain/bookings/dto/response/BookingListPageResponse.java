package com.ticket_online.domain.bookings.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record BookingListPageResponse(
        List<BookingListResponse> content, int page, int size, boolean hasNext) {
    public static BookingListPageResponse from(Page<BookingListResponse> result) {
        return new BookingListPageResponse(
                result.getContent(), result.getNumber(), result.getSize(), result.hasNext());
    }
}
