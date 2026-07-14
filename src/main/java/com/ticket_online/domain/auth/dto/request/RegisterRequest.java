package com.ticket_online.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username cannot be blank")
                @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
                String username,
        @NotBlank(message = "Email cannot be blank") @Email(message = "Email must be valid")
                String email,
        @NotBlank(message = "Password cannot be blank")
                @Size(min = 8, message = "Password must be at least 8 characters")
                String password,
        @NotBlank(message = "Full name cannot be blank") String fullName,
        @NotBlank(message = "Phone number cannot be blank")
                @Pattern(
                        regexp = "^0\\d{9}$",
                        message = "Phone number must be 10 digits and start with 0")
                String phoneNumber) {}
