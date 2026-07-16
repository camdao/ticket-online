package com.ticket_online.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for refreshing access token")
public record RefreshTokenRequest(
        @Schema(
                        description = "Valid refresh token",
                        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Refresh token cannot be blank")
                String refreshToken) {}
