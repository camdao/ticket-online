package com.ticket_online.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UrlConstants {
    PROD_SERVER_URL(""),
    DEV_SERVER_URL(""),
    LOCAL_SERVER_URL("http://localhost:8080"),

    PROD_DOMAIN_URL(""),
    DEV_DOMAIN_URL(""),
    LOCAL_DOMAIN_URL("http://localhost:3000"),
    LOCAL_SECURE_DOMAIN_URL("https://localhost:3000"),
    ;

    private String value;
}
