package com.ticket_online.global.util;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisSeatScripts {

    public static final Duration HOLD_TTL = Duration.ofDays(5L * 60 * 1000);

    private final RedisTemplate<String, String> redisTemplate;

    private static final RedisScript<Long> HOLD_SEATS =
            RedisScript.of("""
                for i = 1, #KEYS do
                    local v = redis.call("GET", KEYS[i])
                    if v and v ~= ARGV[1] then
                        return 0
                    end
                end
                for i = 1, #KEYS do
                    redis.call("SET", KEYS[i], ARGV[1], "PX", ARGV[2])
                end
                return 1
            """, Long.class);

    private static final RedisScript<Long> CHECK_AND_EXTEND_SEATS =
            RedisScript.of("""
            for i = 1, #KEYS do
                local v = redis.call("GET", KEYS[i])
                if not v or v ~= ARGV[1] then
                    return 0
                end
            end
            for i = 1, #KEYS do
                redis.call("PEXPIRE", KEYS[i], ARGV[2])
            end
            return 1
        """, Long.class);


    private String key(Long showId, Long seatId) {
        return "seat:hold:" + showId + ":" + seatId;
    }

    public HoldSeatResult holdSeats(List<Long> seatIds, Long userId, Duration ttl) {
        List<String> keys = seatIds.stream()
                .map(seatId -> key(0L, seatId))
                .toList();

        Long r = redisTemplate.execute(
                HOLD_SEATS,
                keys,
                userId.toString(),
                String.valueOf(ttl.toMillis())
        );

        return r == 1
                ? HoldSeatResult.SUCCESS
                : HoldSeatResult.OWNED_BY_OTHER;
    }

    public HoldSeatResult checkAndExtendSeats(
            Long showId,
            List<Long> seatIds,
            Long userId,
            Duration ttl
    ) {
        List<String> keys = seatIds.stream()
                .map(seatId -> key(showId, seatId))
                .toList();

        Long r = redisTemplate.execute(
                CHECK_AND_EXTEND_SEATS,
                keys,
                userId.toString(),
                String.valueOf(ttl.toMillis())
        );

        return r == 1
                ? HoldSeatResult.SUCCESS
                : HoldSeatResult.OWNED_BY_OTHER;
    }

}
