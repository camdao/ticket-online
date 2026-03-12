package com.ticket_online.domain.auth.dto;

public record RefreshTokenDto(Long memberId, String tokenValue, Long ttl) {}
