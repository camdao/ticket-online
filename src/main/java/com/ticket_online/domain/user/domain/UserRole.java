package com.ticket_online.domain.user.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserRole {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");

    private final String value;
}
