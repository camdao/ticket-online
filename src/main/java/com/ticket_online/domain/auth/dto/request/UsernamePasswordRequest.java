package com.ticket_online.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for user login")
public record UsernamePasswordRequest(
        @Schema(
                        description = "Username or email",
                        example = "johndoe",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "Username cannot be null")
                String username,
        @Schema(
                        description = "User password",
                        example = "SecurePass123",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "Password cannot be null")
                String password) {}
