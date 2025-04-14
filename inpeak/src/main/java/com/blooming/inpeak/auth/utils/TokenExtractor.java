package com.blooming.inpeak.auth.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenExtractor {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public String extractAccessToken(HttpServletRequest request) {
        // 쿠키에서 토큰 추출 시도
        String accessToken = extractCookie(request, ACCESS_TOKEN_COOKIE_NAME);

        // 쿠키에 토큰이 없다면 Authorization 헤더에서 Bearer 토큰 추출 시도
        if (accessToken == null) {
            accessToken = extractBearerToken(request);
        }

        return accessToken;
    }

    public String extractRefreshToken(HttpServletRequest request) {
        // 쿠키에서 토큰 추출 시도
        String refreshToken = extractCookie(request, REFRESH_TOKEN_COOKIE_NAME);

        // 쿠키에 토큰이 없다면 Authorization 헤더에서 Bearer 토큰 추출 시도
        if (refreshToken == null) {
            refreshToken = extractBearerToken(request);
        }

        return refreshToken;
    }

    public String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String extractBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
