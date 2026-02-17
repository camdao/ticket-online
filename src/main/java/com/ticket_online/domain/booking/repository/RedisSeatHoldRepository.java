package com.ticket_online.domain.booking.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class RedisSeatHoldRepository {

    private final StringRedisTemplate redisTemplate;

    private String key(Long showId, Long seatId) {
        return "seat:hold:" + showId + ":" + seatId;
    }

    public boolean hold(Long showId, Long seatId, Long userId, Duration ttl) {
        return Boolean.TRUE.equals(
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(key(showId, seatId), String.valueOf(userId), ttl));
    }

    public void release(Long showId, Long seatId) {
        redisTemplate.delete(key(showId, seatId));
    }

    public boolean isHeldByUser(Long showId, Long seatId, Long userId) {
        String holder = redisTemplate.opsForValue().get(key(showId, seatId));
        return holder != null && holder.equals(String.valueOf(userId));
    }
}
