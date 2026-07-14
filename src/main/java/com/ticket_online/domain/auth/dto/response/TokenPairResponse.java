package com.ticket_online.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        @JsonProperty("expiresIn") Long expiresIn) {

    public static TokenPairResponse from(String accessToken, String refreshToken, Long expiresIn) {
        return new TokenPairResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
