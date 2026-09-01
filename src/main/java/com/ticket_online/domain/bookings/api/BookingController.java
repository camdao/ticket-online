package com.ticket_online.domain.bookings.api;

import com.ticket_online.domain.bookings.application.BookingService;
import com.ticket_online.domain.bookings.dto.request.CreateBookingRequest;
import com.ticket_online.domain.bookings.dto.response.BookingDetailResponse;
import com.ticket_online.domain.bookings.dto.response.BookingListPageResponse;
import com.ticket_online.domain.bookings.dto.response.BookingResponse;
import com.ticket_online.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookings", description = "Cinema ticket booking and seat reservation endpoints")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtil securityUtil;

    @Operation(
            summary = "Create a booking",
            description =
                    "Creates a ticket booking with seat reservation and payment URL generation in a"
                            + " single atomic operation. Validates showtime and seats, acquires seat"
                            + " locks in Redis, creates the booking, and returns a payment URL. The"
                            + " booking will be in PENDING status until payment is confirmed. Requires"
                            + " authentication.")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request, HttpServletRequest httpRequest) {
        Long userId = securityUtil.getCurrentUserId();
        BookingResponse response = bookingService.createBooking(request, userId, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get user booking history",
            description =
                    "Retrieves a paginated list of confirmed bookings for the authenticated user."
                            + " Results are sorted by creation date in descending order. Requires"
                            + " authentication.")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingListPageResponse> getUserBookings(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        Long userId = securityUtil.getCurrentUserId();
        BookingListPageResponse response = bookingService.getUserBookings(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get booking details",
            description =
                    "Retrieves detailed information about a specific booking including movie, cinema,"
                            + " screen, showtime, seats, and payment information. Users can only access"
                            + " their own bookings. Requires authentication.")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDetailResponse> getBookingDetail(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        BookingDetailResponse response = bookingService.getBookingDetail(id, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cancel a booking",
            description =
                    "Cancels a booking and releases the reserved seats. Only PENDING bookings can be"
                            + " cancelled. CONFIRMED bookings cannot be cancelled through this endpoint."
                            + " Users can only cancel their own bookings. Requires authentication.")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok().build();
    }
}
