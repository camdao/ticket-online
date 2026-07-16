package com.ticket_online.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing access and refresh tokens")
public record TokenPairResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                String accessToken,
        @Schema(
                        description = "JWT refresh token for obtaining new access tokens",
                        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                String refreshToken,
        @Schema(description = "Token type", example = "Bearer") String tokenType,
        @Schema(description = "Access token expiration time in seconds", example = "3600")
                @JsonProperty("expiresIn")
                Long expiresIn) {

    public static TokenPairResponse from(String accessToken, String refreshToken, Long expiresIn) {
        return new TokenPairResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
