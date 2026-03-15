package com.ticket_online.global.util;

import static com.ticket_online.global.common.constants.SecurityConstants.*;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final SpringEnvironmentUtil springEnvironmentUtil;

    public HttpHeaders generateTokenCookies(String accessToken, String refreshToken) {

        String sameSite = determineSameSitePolicy();

        ResponseCookie accessTokenCookie =
                ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                        .path("/")
                        .secure(true)
                        .sameSite(sameSite)
                        .httpOnly(true)
                        .build();

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                        .path("/")
                        .secure(true)
                        .sameSite(sameSite)
                        .httpOnly(true)
                        .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return headers;
    }

    private String determineSameSitePolicy() {
        if (springEnvironmentUtil.isProdProfile()) {
            return Cookie.SameSite.STRICT.attributeValue();
        }
        return Cookie.SameSite.NONE.attributeValue();
    }

    public HttpHeaders deleteTokenCookies() {

        String sameSite = determineSameSitePolicy();

        ResponseCookie accessTokenCookie =
                ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                        .path("/")
                        .maxAge(0)
                        .secure(true)
                        .sameSite(sameSite)
                        .httpOnly(true)
                        .build();

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                        .path("/")
                        .maxAge(0)
                        .secure(true)
                        .sameSite(sameSite)
                        .httpOnly(true)
                        .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return headers;
    }

    public HttpHeaders clearTokenCookies() {
        HttpHeaders headers = new HttpHeaders();

        ResponseCookie accessTokenCookie =
                ResponseCookie.from("accessToken", "")
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(0) // Expire immediately
                        .build();

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(0) // Expire immediately
                        .build();

        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return headers;
    }
}
