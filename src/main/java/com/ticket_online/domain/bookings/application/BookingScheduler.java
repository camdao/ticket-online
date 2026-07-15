package com.ticket_online.domain.bookings.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedRate = 60000) // Run every minute
    public void expireOldBookings() {
        try {
            bookingService.expireOldBookings();
        } catch (Exception e) {
            log.error("Error expiring old bookings", e);
        }
    }
}
