package com.ticket_online.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UrlConstants {
    PROD_SERVER_URL(""),
    DEV_SERVER_URL(""),
    LOCAL_SERVER_URL("http://localhost:8081"),

    PROD_DOMAIN_URL("https://ticket-client-gold.vercel.app"),
    DEV_DOMAIN_URL(""),
    LOCAL_DOMAIN_URL("http://localhost:3001"),
    LOCAL_SECURE_DOMAIN_URL("https://localhost:3001"),
    ;

    private String value;
}
