package com.ticket_online.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenResponse(
        String accessToken, String tokenType, @JsonProperty("expiresIn") Long expiresIn) {

    public static AccessTokenResponse from(String accessToken, Long expiresIn) {
        return new AccessTokenResponse(accessToken, "Bearer", expiresIn);
    }
}
