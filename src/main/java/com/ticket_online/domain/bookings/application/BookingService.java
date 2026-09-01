package com.ticket_online.domain.bookings.application;

import com.ticket_online.domain.bookings.dao.BookingDetailRepository;
import com.ticket_online.domain.bookings.dao.BookingRepository;
import com.ticket_online.domain.bookings.domain.Booking;
import com.ticket_online.domain.bookings.domain.BookingDetail;
import com.ticket_online.domain.bookings.dto.request.CreateBookingRequest;
import com.ticket_online.domain.bookings.dto.response.*;
import com.ticket_online.domain.payments.application.PaymentService;
import com.ticket_online.domain.payments.domain.Payment;
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
    private final PaymentService paymentService;

    private static final int SEAT_HOLD_TTL_SECONDS = 300; // 5 minutes

    @Transactional
    public BookingResponse createBooking(
            CreateBookingRequest request, Long userId, String ipAddress) {
        Showtime showtime =
                showtimeRepository
                        .findById(request.getShowtimeId())
                        .orElseThrow(() -> new CustomException(ErrorCode.SHOWTIME_NOT_FOUND));
        List<Seat> seats = seatRepository.findByIdIn(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new CustomException(ErrorCode.SEATS_NOT_FOUND);
        }
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        redisSeatScripts.holdSeats(
                request.getSeatIds(), request.getShowtimeId(), userId, SEAT_HOLD_TTL_SECONDS);

        BigDecimal totalAmount = calculateTotalAmount(showtime, seats);

        String bookingCode = generateBookingCode();

        Booking booking =
                Booking.createBooking(
                        bookingCode,
                        user,
                        showtime,
                        totalAmount,
                        request.getCustomerEmail(),
                        request.getCustomerPhone());

        booking = bookingRepository.save(booking);

        Payment payment =
                paymentService.createPayment(booking, request.getPaymentMethod(), ipAddress);

        log.info(
                "Created booking {} with {} seats, transaction ID: {}",
                bookingCode,
                seats.size(),
                payment.getTransactionId());

        return BookingResponse.from(payment);
    }

    public BookingListPageResponse getUserBookings(Long userId, Pageable pageable) {
        Page<BookingListResponse> result = bookingRepository.findUserBookings(userId, pageable);

        return BookingListPageResponse.from(result);
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

    private BigDecimal calculateTotalAmount(Showtime showtime, List<Seat> seats) {

        return seats.stream()
                .map(seat -> showtime.getBasePrice().add(BigDecimal.valueOf(seat.getSurcharge())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void createBookingDetails(Booking booking, Showtime showtime, List<Seat> seats) {

        List<BookingDetail> details =
                seats.stream()
                        .map(
                                seat -> {
                                    BigDecimal price =
                                            showtime.getBasePrice()
                                                    .add(BigDecimal.valueOf(seat.getSurcharge()));

                                    return BookingDetail.createBookingDetail(booking, seat, price);
                                })
                        .toList();

        bookingDetailRepository.saveAll(details);
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
