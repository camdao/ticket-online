package com.ticket_online.domain.auth.dto;

import com.ticket_online.domain.user.domain.UserRole;

public record AccessTokenDto(Long memberId, UserRole userRole, String tokenValue) {}
