package com.ticket_online.domain.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PayProvider {
    VNPAY("VNPAY"),
    MOMO("MOMO"),
    FAKE("FAKE");

    private final String value;
}
