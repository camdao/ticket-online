package com.ticket_online.domain.bookings.domain;

public enum BookingStatus {
    PENDING,    // Đang chờ thanh toán
    CONFIRMED,  // Đã thanh toán thành công
    CANCELLED,  // Đã hủy
    EXPIRED     // Hết hạn (quá 15 phút chưa thanh toán)
}