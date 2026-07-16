package com.ticket_online.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for logout operation")
public record LogoutResponse(
        @Schema(description = "Logout status message", example = "Logout successful")
                String message) {

    public static LogoutResponse success() {
        return new LogoutResponse("Logout successful");
    }
}
