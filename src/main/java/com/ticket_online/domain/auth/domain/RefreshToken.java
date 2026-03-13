package com.ticket_online.domain.auth.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "refresh_token")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RefreshToken {

    @Id private Long memberId;
    private String token;
    private long ttl;

    @Builder
    public RefreshToken(Long memberId, String token, long ttl) {
        this.memberId = memberId;
        this.token = token;
        this.ttl = ttl;
    }

    public static RefreshToken createRefreshToken(Long memberId, String token, long ttl) {
        return RefreshToken.builder().memberId(memberId).token(token).ttl(ttl).build();
    }
}
