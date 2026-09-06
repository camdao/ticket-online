package com.ticket_online.domain.bookings.application;

import com.ticket_online.domain.bookings.dao.BookingDetailProjection;
import com.ticket_online.domain.bookings.dao.BookingDetailRepository;
import com.ticket_online.domain.bookings.dao.BookingListProjection;
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

    // TODO: Add idempotency to prevent duplicate bookings when the same request is retried.
    // TODO: Check business rule to prevent a user from creating multiple active bookings
    //       for the same seat and showtime.
    // TODO: rate condition booking, use can add schema seat_showtime
    // TODO: redis hold seat can can make api other, because redis can hold pool thread db, trade
    // off retry booking
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

        if (bookingDetailRepository.existsActiveBooking(
                request.getShowtimeId(), request.getSeatIds())) {
            throw new CustomException(ErrorCode.SEATS_ALREADY_BOOKED);
        }

        redisSeatScripts.holdSeats(request.getSeatIds(), request.getShowtimeId(), userId);
        // roll back if @Transactional Error
        try {
            BigDecimal totalAmount = calculateTotalAmount(showtime, seats);

            String bookingCode = generateBookingCode();

            Booking booking = Booking.createBooking(bookingCode, user, showtime, totalAmount);

            booking = bookingRepository.save(booking);

            createBookingDetails(booking, showtime, seats);

            Payment payment =
                    paymentService.createPayment(booking, request.getPaymentMethod(), ipAddress);

            log.info(
                    "Created booking {} with {} seats, transaction ID: {}",
                    bookingCode,
                    seats.size(),
                    payment.getTransactionId());

            return BookingResponse.from(payment);
        } catch (Exception e) {

            redisSeatScripts.releaseSeats(request.getShowtimeId(), request.getSeatIds());

            throw e;
        }
    }

    public BookingListPageResponse getUserBookings(Long userId, Pageable pageable) {

        Page<BookingListProjection> result = bookingRepository.findUserBookings(userId, pageable);

        return toBookingListPageResponse(result);
    }

    public BookingDetailResponse getBookingDetail(Long bookingId, Long userId) {

        List<BookingDetailProjection> projections =
                bookingRepository.findBookingDetail(bookingId, userId);

        if (projections.isEmpty()) {
            throw new CustomException(ErrorCode.BOOKING_NOT_FOUND);
        }

        return toBookingDetailResponse(projections);
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BOOKING_NOT_FOUND);
        }

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
        List<Long> seatIds = bookingDetailRepository.findSeatIdsByBookingId(bookingId);
        redisSeatScripts.releaseSeats(booking.getShowtime().getId(), seatIds);
    }

    @Transactional
    public void expireOldBookings() {
        List<Booking> expiredBookings =
                bookingRepository.findExpiredPendingBookings(LocalDateTime.now());

        for (Booking booking : expiredBookings) {
            booking.expire();
            bookingRepository.save(booking);

            List<Long> seatIds = bookingDetailRepository.findSeatIdsByBookingId(booking.getId());
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

    private BookingListPageResponse toBookingListPageResponse(Page<BookingListProjection> result) {

        return new BookingListPageResponse(
                result.getContent().stream()
                        .map(
                                p ->
                                        new BookingListResponse(
                                                p.getId(),
                                                p.getBookingCode(),
                                                p.getStatus(),
                                                p.getMovieTitle()))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.hasNext());
    }

    private BookingDetailResponse toBookingDetailResponse(
            List<BookingDetailProjection> projections) {

        BookingDetailProjection first = projections.get(0);

        List<SeatBookingResponse> seats =
                projections.stream()
                        .map(
                                p ->
                                        new SeatBookingResponse(
                                                p.getSeatId(),
                                                p.getSeatRow(),
                                                p.getSeatNumber(),
                                                p.getSeatType(),
                                                p.getPrice()))
                        .toList();

        return new BookingDetailResponse(
                first.getBookingId(),
                first.getBookingCode(),
                first.getMovieTitle(),
                first.getMovieImageUrl(),
                first.getTotalAmount(),
                first.getStatus(),
                first.getCreatedAt(),
                first.getConfirmedAt(),
                first.getStartTime(),
                first.getEndTime(),
                seats);
    }
}
