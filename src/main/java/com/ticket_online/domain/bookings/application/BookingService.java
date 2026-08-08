package com.ticket_online.domain.bookings.application;

import com.ticket_online.domain.bookings.dao.BookingDetailRepository;
import com.ticket_online.domain.bookings.dao.BookingRepository;
import com.ticket_online.domain.bookings.domain.Booking;
import com.ticket_online.domain.bookings.domain.BookingDetail;
import com.ticket_online.domain.bookings.domain.BookingStatus;
import com.ticket_online.domain.bookings.dto.request.CreateBookingRequest;
import com.ticket_online.domain.bookings.dto.response.*;
import com.ticket_online.domain.payments.application.VnpayService;
import com.ticket_online.domain.payments.dao.PaymentRepository;
import com.ticket_online.domain.payments.domain.Payment;
import com.ticket_online.domain.seats.dao.SeatRepository;
import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.showtimes.dao.ShowtimeRepository;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.config.vnpay.VnpayProperties;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.util.RedisSeatScripts;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final RedisSeatScripts redisSeatScripts;
    private final PaymentRepository paymentRepository;
    private final VnpayService vnpayService;
    private final VnpayProperties vnpayProperties;

    private static final int SEAT_HOLD_TTL_SECONDS = 300; // 5 minutes

    @Transactional
    public BookingResponse createBooking(
            CreateBookingRequest request, Long userId, HttpServletRequest httpRequest) {
        // 1. Validate showtime exists
        Showtime showtime =
                showtimeRepository
                        .findById(request.getShowtimeId())
                        .orElseThrow(() -> new CustomException(ErrorCode.SHOWTIME_NOT_FOUND));

        // 2. Validate seats exist
        List<Seat> seats = seatRepository.findByIdIn(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new CustomException(ErrorCode.SEATS_NOT_FOUND);
        }

        // 3. Acquire seat lock in Redis (will throw exception if seats already held)
        redisSeatScripts.holdSeats(
                request.getSeatIds(), request.getShowtimeId(), userId, SEAT_HOLD_TTL_SECONDS);

        // Get user entity
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Calculate total amount
        BigDecimal totalAmount =
                seats.stream()
                        .map(
                                seat ->
                                        showtime.getBasePrice()
                                                .add(BigDecimal.valueOf(seat.getSurcharge())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate unique booking code
        String bookingCode = generateBookingCode();

        // 4. Create booking
        Booking booking =
                Booking.createBooking(
                        bookingCode,
                        user,
                        showtime,
                        totalAmount,
                        request.getCustomerEmail(),
                        request.getCustomerPhone());
        booking = bookingRepository.save(booking);

        // Create booking details
        Booking finalBooking = booking;
        List<BookingDetail> bookingDetails =
                seats.stream()
                        .map(
                                seat -> {
                                    BigDecimal seatPrice =
                                            showtime.getBasePrice()
                                                    .add(BigDecimal.valueOf(seat.getSurcharge()));
                                    return BookingDetail.createBookingDetail(
                                            finalBooking, seat, seatPrice);
                                })
                        .toList();
        bookingDetailRepository.saveAll(bookingDetails);

        // 5. Generate payment URL
        String transactionId = generateTransactionId();
        String ipAddress = getClientIp(httpRequest);
        String orderInfo = "Thanh toan ve phim - Booking: " + booking.getBookingCode();

        String paymentUrl =
                vnpayService.createPaymentUrl(
                        transactionId,
                        totalAmount.longValue(),
                        orderInfo,
                        vnpayProperties.returnUrl(),
                        ipAddress);

        // Create payment record
        Payment payment =
                Payment.createPayment(
                        booking,
                        transactionId,
                        request.getPaymentMethod(),
                        totalAmount,
                        paymentUrl);
        paymentRepository.save(payment);

        log.info(
                "Created booking {} with {} seats, transaction ID: {}",
                bookingCode,
                seats.size(),
                transactionId);

        return buildBookingResponse(booking, seats, showtime, paymentUrl, transactionId);
    }

    public Page<BookingListResponse> getUserBookings(Long userId, Pageable pageable) {
        // Only return CONFIRMED bookings by default as per API design
        Page<Booking> bookings =
                bookingRepository.findByUserIdAndStatus(userId, BookingStatus.CONFIRMED, pageable);
        return bookings.map(this::buildBookingListResponse);
    }

    public BookingDetailResponse getBookingDetail(Long bookingId, Long userId) {
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BOOKING_NOT_FOUND));

        // Verify ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOOKING_NOT_FOUND);
        }

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(bookingId);

        return buildBookingDetailResponse(booking, bookingDetails);
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BOOKING_NOT_FOUND));

        // Verify ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOOKING_NOT_FOUND);
        }

        // Check if booking can be cancelled
        if (!booking.canBeCancelled()) {
            if (booking.isConfirmed()) {
                throw new CustomException(ErrorCode.BOOKING_CANNOT_CANCEL);
            } else if (booking.isCancelled()) {
                throw new CustomException(ErrorCode.BOOKING_ALREADY_CANCELLED);
            } else if (booking.isExpired()) {
                throw new CustomException(ErrorCode.BOOKING_EXPIRED);
            }
        }

        booking.cancel();
        bookingRepository.save(booking);

        // Release seats from Redis if still held
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);
        List<Long> seatIds = details.stream().map(bd -> bd.getSeat().getId()).toList();
        redisSeatScripts.releaseSeats(booking.getShowtime().getId(), seatIds);
    }

    @Transactional
    public void expireOldBookings() {
        List<Booking> expiredBookings =
                bookingRepository.findExpiredPendingBookings(LocalDateTime.now());

        for (Booking booking : expiredBookings) {
            booking.expire();
            bookingRepository.save(booking);

            // Release seats
            List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
            List<Long> seatIds = details.stream().map(bd -> bd.getSeat().getId()).toList();
            redisSeatScripts.releaseSeats(booking.getShowtime().getId(), seatIds);
        }

        if (!expiredBookings.isEmpty()) {
            log.info("Expired {} bookings", expiredBookings.size());
        }
    }

    private String generateBookingCode() {
        String prefix = "BK";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int) (Math.random() * 1000));
        return prefix + timestamp.substring(timestamp.length() - 10) + random;
    }

    private String generateTransactionId() {
        String prefix = "PAY";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8);
        return prefix + timestamp.substring(timestamp.length() - 10) + random;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private BookingResponse buildBookingResponse(
            Booking booking,
            List<Seat> seats,
            Showtime showtime,
            String paymentUrl,
            String transactionId) {
        List<SeatDto> seatDtos =
                seats.stream()
                        .map(
                                seat ->
                                        SeatDto.builder()
                                                .id(seat.getId())
                                                .row(seat.getRow())
                                                .number(seat.getNumber())
                                                .type(seat.getType())
                                                .price(
                                                        showtime.getBasePrice()
                                                                .add(
                                                                        BigDecimal.valueOf(
                                                                                seat
                                                                                        .getSurcharge())))
                                                .build())
                        .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUser().getId())
                .showtimeId(showtime.getId())
                .movieTitle(showtime.getMovie().getTitle())
                .cinemaName(showtime.getCinema().getName())
                .screenName(showtime.getRoom().getName())
                .showtime(showtime.getStartTime())
                .seats(seatDtos)
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentUrl(paymentUrl)
                .transactionId(transactionId)
                .createdAt(booking.getCreatedAt())
                .expiresAt(booking.getExpiresAt())
                .confirmedAt(booking.getConfirmedAt())
                .build();
    }

    private BookingListResponse buildBookingListResponse(Booking booking) {
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());

        return BookingListResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .movieTitle(booking.getShowtime().getMovie().getTitle())
                .moviePosterUrl(booking.getShowtime().getMovie().getImageUrl())
                .cinemaName(booking.getShowtime().getCinema().getName())
                .screenName(booking.getShowtime().getRoom().getName())
                .showtime(booking.getShowtime().getStartTime())
                .seatCount(details.size())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .confirmedAt(booking.getConfirmedAt())
                .build();
    }

    private BookingDetailResponse buildBookingDetailResponse(
            Booking booking, List<BookingDetail> bookingDetails) {
        Showtime showtime = booking.getShowtime();

        List<SeatDto> seatDtos =
                bookingDetails.stream()
                        .map(
                                bd ->
                                        SeatDto.builder()
                                                .id(bd.getSeat().getId())
                                                .row(bd.getSeat().getRow())
                                                .number(bd.getSeat().getNumber())
                                                .type(bd.getSeat().getType())
                                                .price(bd.getPrice())
                                                .build())
                        .toList();

        return BookingDetailResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUser().getId())
                .movie(
                        BookingDetailResponse.MovieInfo.builder()
                                .id(showtime.getMovie().getId())
                                .title(showtime.getMovie().getTitle())
                                .posterUrl(showtime.getMovie().getImageUrl())
                                .duration(showtime.getMovie().getDuration())
                                .ageRating(showtime.getMovie().getRating())
                                .build())
                .cinema(
                        BookingDetailResponse.CinemaInfo.builder()
                                .id(showtime.getCinema().getId())
                                .name(showtime.getCinema().getName())
                                .address(
                                        showtime.getCinema().getAddress()
                                                + ", "
                                                + showtime.getCinema().getDistrict()
                                                + ", "
                                                + showtime.getCinema().getCity())
                                .build())
                .screen(
                        BookingDetailResponse.ScreenInfo.builder()
                                .id(showtime.getRoom().getId())
                                .name(showtime.getRoom().getName())
                                .type(showtime.getRoom().getRoomType())
                                .build())
                .showtime(showtime.getStartTime())
                .seats(seatDtos)
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentStatus(booking.isConfirmed() ? "SUCCESS" : "PENDING")
                .createdAt(booking.getCreatedAt())
                .confirmedAt(booking.getConfirmedAt())
                .expiresAt(booking.getExpiresAt())
                .build();
    }
}
