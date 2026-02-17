package com.ticket_online.domain.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PayStatus {
    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    private final String value;
}
