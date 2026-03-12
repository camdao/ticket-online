package com.ticket_online;

import com.ticket_online.global.config.redis.RedisConfig;
import com.ticket_online.global.config.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@EnableConfigurationProperties({RedisProperties.class})
@Import({RedisConfig.class})
public class TestRedisConfig {}