package com.ticket_online.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for user registration")
public record RegisterRequest(
        @Schema(
                        description = "Username for the account",
                        example = "johndoe",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Username cannot be blank")
                @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
                String username,
        @Schema(
                        description = "Email address",
                        example = "john.doe@example.com",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Email cannot be blank")
                @Email(message = "Email must be valid")
                String email,
        @Schema(
                        description = "Password (minimum 8 characters)",
                        example = "SecurePass123",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Password cannot be blank")
                @Size(min = 8, message = "Password must be at least 8 characters")
                String password,
        @Schema(
                        description = "Full name of the user",
                        example = "John Doe",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Full name cannot be blank")
                String fullName,
        @Schema(
                        description = "Phone number (10 digits starting with 0)",
                        example = "0123456789",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Phone number cannot be blank")
                @Pattern(
                        regexp = "^0\\d{9}$",
                        message = "Phone number must be 10 digits and start with 0")
                String phoneNumber) {}
