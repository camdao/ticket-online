package com.ticket_online.global.util;

import com.ticket_online.global.error.exception.CustomException;
import com.ticket_online.global.error.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSeatScripts {

    private final RedisTemplate<String, String> redisTemplate;

    private String key(Long showId, Long seatId) {
        return "seat:hold:" + showId + ":" + seatId;
    }

    public HoldSeatResult holdSeats(List<Long> seatIds, Long showId, Long userId, int ttlSeconds) {
        List<String> keys = seatIds.stream().map(seatId -> key(showId, seatId)).toList();

        RedisScript<Long> HOLD_SEATS =
                RedisScript.of(
                        """
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
            """,
                        Long.class);

        Long r =
                redisTemplate.execute(
                        HOLD_SEATS, keys, userId.toString(), String.valueOf(ttlSeconds * 1000));
        if (r == 0) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_HELD);
        }
        return HoldSeatResult.SUCCESS;
    }

    public void releaseSeats(Long showId, List<Long> seatIds) {

        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }

        List<String> keys = seatIds.stream().map(seatId -> key(showId, seatId)).toList();

        RedisScript<Long> RELEASE_SEATS =
                RedisScript.of(
                        """
                for i = 1, #KEYS do
                    redis.call("DEL", KEYS[i])
                end
                return #KEYS
            """,
                        Long.class);

        redisTemplate.execute(RELEASE_SEATS, keys);
    }
}
