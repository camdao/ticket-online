package com.ticket_online.global.config.vnpay;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vnpay")
public record VnpayProperties(String tmnCode, String hashSecret, String payUrl, String returnUrl) {}
