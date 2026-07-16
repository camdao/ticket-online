package com.ticket_online.domain.bookings.api;

import com.ticket_online.domain.bookings.application.BookingService;
import com.ticket_online.domain.bookings.domain.BookingStatus;
import com.ticket_online.domain.bookings.dto.request.CreateBookingRequest;
import com.ticket_online.domain.bookings.dto.request.HoldSeatsRequest;
import com.ticket_online.domain.bookings.dto.response.BookingDetailResponse;
import com.ticket_online.domain.bookings.dto.response.BookingListResponse;
import com.ticket_online.domain.bookings.dto.response.BookingResponse;
import com.ticket_online.domain.bookings.dto.response.HoldSeatsResponse;
import com.ticket_online.global.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtil securityUtil;

    @PostMapping("/hold-seats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HoldSeatsResponse> holdSeats(
            @Valid @RequestBody HoldSeatsRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        HoldSeatsResponse response = bookingService.holdSeats(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        BookingResponse response = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookingListResponse>> getUserBookings(
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        Long userId = securityUtil.getCurrentUserId();
        Page<BookingListResponse> response =
                bookingService.getUserBookings(userId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDetailResponse> getBookingDetail(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        BookingDetailResponse response = bookingService.getBookingDetail(id, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok().build();
    }
}
