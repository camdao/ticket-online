package com.ticket_online.global.config.properties;

import com.ticket_online.global.config.redis.RedisProperties;
import com.ticket_online.global.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
    RedisProperties.class,
    JwtProperties.class,
})
@Configuration
public class PropertiesConfig {}
