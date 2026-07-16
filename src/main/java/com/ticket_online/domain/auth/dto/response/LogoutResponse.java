package com.ticket_online.domain.auth.dto.response;

public record LogoutResponse(String message) {

    public static LogoutResponse success() {
        return new LogoutResponse("Logout successful");
    }
}