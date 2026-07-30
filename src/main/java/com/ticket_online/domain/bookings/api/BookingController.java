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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Bookings", description = "Cinema ticket booking and seat reservation endpoints")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtil securityUtil;

    @Operation(
            summary = "Hold seats temporarily",
            description =
                    "Reserves selected seats for 5 minutes to allow the user to complete the booking"
                            + " process. Returns a hold token that must be used to create the"
                            + " booking. Requires authentication.")
    @PostMapping("/hold-seats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HoldSeatsResponse> holdSeats(
            @Valid @RequestBody HoldSeatsRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        HoldSeatsResponse response = bookingService.holdSeats(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Create a booking",
            description =
                    "Creates a ticket booking from previously held seats. Requires a valid hold token"
                            + " obtained from the hold-seats endpoint. The booking will be in PENDING"
                            + " status until payment is confirmed. Requires authentication.")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        BookingResponse response = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get user booking history",
            description =
                    "Retrieves a paginated list of all bookings for the authenticated user. Can be"
                            + " filtered by booking status (PENDING, CONFIRMED, CANCELLED, EXPIRED)."
                            + " Results are sorted by creation date in descending order. Requires"
                            + " authentication.")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookingListResponse>> getUserBookings(
            @Parameter(description = "Filter by booking status") @RequestParam(required = false)
                    BookingStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        Long userId = securityUtil.getCurrentUserId();
        Page<BookingListResponse> response =
                bookingService.getUserBookings(userId, status, pageable);
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
