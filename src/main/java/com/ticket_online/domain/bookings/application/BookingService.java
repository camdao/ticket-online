package com.ticket_online.domain.bookings.application;

import com.ticket_online.domain.bookings.dao.BookingDetailRepository;
import com.ticket_online.domain.bookings.dao.BookingRepository;
import com.ticket_online.domain.bookings.domain.Booking;
import com.ticket_online.domain.bookings.domain.BookingDetail;
import com.ticket_online.domain.bookings.domain.BookingStatus;
import com.ticket_online.domain.bookings.dto.request.CreateBookingRequest;
import com.ticket_online.domain.bookings.dto.request.HoldSeatsRequest;
import com.ticket_online.domain.bookings.dto.response.*;
import com.ticket_online.domain.seats.dao.SeatRepository;
import com.ticket_online.domain.seats.domain.Seat;
import com.ticket_online.domain.showtimes.dao.ShowtimeRepository;
import com.ticket_online.domain.showtimes.domain.Showtime;
import com.ticket_online.domain.user.dao.UserRepository;
import com.ticket_online.domain.user.domain.User;
import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import com.ticket_online.global.util.RedisSeatScripts;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, String> redisTemplate;

    private static final int SEAT_HOLD_TTL_SECONDS = 300; // 5 minutes
    private static final String HOLD_TOKEN_PREFIX = "hold_token:";

    @Transactional
    public HoldSeatsResponse holdSeats(HoldSeatsRequest request, Long userId) {
        // Validate showtime exists
        Showtime showtime =
                showtimeRepository
                        .findById(request.getShowtimeId())
                        .orElseThrow(() -> new CustomException(ErrorCode.SHOWTIME_NOT_FOUND));

        // Validate seats exist
        List<Seat> seats = seatRepository.findByIdIn(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new CustomException(ErrorCode.SEATS_NOT_FOUND);
        }

        // Hold seats in Redis
        redisSeatScripts.holdSeats(
                request.getSeatIds(), request.getShowtimeId(), userId, SEAT_HOLD_TTL_SECONDS);

        // Generate hold token and store in Redis
        String holdToken = UUID.randomUUID().toString();
        String holdTokenKey = HOLD_TOKEN_PREFIX + holdToken;

        // Store hold info: userId, showtimeId, seatIds
        String holdInfo =
                userId
                        + ":"
                        + request.getShowtimeId()
                        + ":"
                        + String.join(
                                ",", request.getSeatIds().stream().map(String::valueOf).toList());
        redisTemplate
                .opsForValue()
                .set(holdTokenKey, holdInfo, java.time.Duration.ofSeconds(SEAT_HOLD_TTL_SECONDS));

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(SEAT_HOLD_TTL_SECONDS);

        return HoldSeatsResponse.builder()
                .holdToken(holdToken)
                .showtimeId(request.getShowtimeId())
                .seatIds(request.getSeatIds())
                .expiresAt(expiresAt)
                .remainingSeconds(SEAT_HOLD_TTL_SECONDS)
                .build();
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        // Validate hold token
        String holdTokenKey = HOLD_TOKEN_PREFIX + request.getHoldToken();
        String holdInfo = redisTemplate.opsForValue().get(holdTokenKey);

        if (holdInfo == null) {
            throw new CustomException(ErrorCode.INVALID_HOLD_TOKEN);
        }

        // Parse hold info
        String[] parts = holdInfo.split(":");
        Long holdUserId = Long.parseLong(parts[0]);
        Long holdShowtimeId = Long.parseLong(parts[1]);
        List<Long> holdSeatIds =
                List.of(parts[2].split(",")).stream().map(Long::parseLong).toList();

        // Validate hold token belongs to current user
        if (!holdUserId.equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_HOLD_TOKEN);
        }

        // Validate request matches hold info
        if (!holdShowtimeId.equals(request.getShowtimeId())
                || !holdSeatIds.equals(request.getSeatIds())) {
            throw new CustomException(ErrorCode.INVALID_HOLD_TOKEN);
        }

        // Get entities
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Showtime showtime =
                showtimeRepository
                        .findById(request.getShowtimeId())
                        .orElseThrow(() -> new CustomException(ErrorCode.SHOWTIME_NOT_FOUND));

        List<Seat> seats = seatRepository.findByIdIn(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new CustomException(ErrorCode.SEATS_NOT_FOUND);
        }

        // Calculate total amount
        BigDecimal totalAmount =
                seats.stream()
                        .map(
                                seat ->
                                        showtime.getBasePrice()
                                                .add(BigDecimal.valueOf(seat.getBasePrice())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate unique booking code
        String bookingCode = generateBookingCode();

        // Create booking
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
                                                    .add(BigDecimal.valueOf(seat.getBasePrice()));
                                    return BookingDetail.createBookingDetail(
                                            finalBooking, seat, seatPrice);
                                })
                        .toList();
        bookingDetailRepository.saveAll(bookingDetails);

        // Delete hold token (it's been used)
        redisTemplate.delete(holdTokenKey);

        return buildBookingResponse(booking, seats, showtime);
    }

    public Page<BookingListResponse> getUserBookings(
            Long userId, BookingStatus status, Pageable pageable) {
        Page<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            bookings = bookingRepository.findByUserId(userId, pageable);
        }

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

    private BookingResponse buildBookingResponse(
            Booking booking, List<Seat> seats, Showtime showtime) {
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
                                                                                        .getBasePrice())))
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
                .moviePosterUrl(booking.getShowtime().getMovie().getPosterUrl())
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
                                .posterUrl(showtime.getMovie().getPosterUrl())
                                .duration(showtime.getMovie().getDuration())
                                .ageRating(showtime.getMovie().getAgeRating())
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
