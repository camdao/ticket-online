package com.ticket_online.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing a new access token")
public record AccessTokenResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                String accessToken,
        @Schema(description = "Token type", example = "Bearer") String tokenType,
        @Schema(description = "Access token expiration time in seconds", example = "3600")
                @JsonProperty("expiresIn")
                Long expiresIn) {

    public static AccessTokenResponse from(String accessToken, Long expiresIn) {
        return new AccessTokenResponse(accessToken, "Bearer", expiresIn);
    }
}
