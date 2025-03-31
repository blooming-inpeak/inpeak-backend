package com.blooming.inpeak.auth.service;

import com.blooming.inpeak.auth.domain.RefreshToken;
import com.blooming.inpeak.auth.dto.TokenResponse;
import com.blooming.inpeak.auth.repository.RefreshTokenRepository;
import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    @Value("${jwt.accessToken.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refreshToken.expiration}")
    private long refreshTokenExpiration;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void logout(Long memberId, HttpServletResponse response) {
        // DB에서 리프레시 토큰 삭제
        refreshTokenRepository.deleteByMemberId(memberId);

        // 쿠키 무효화
        removeTokenCookies(response);
    }

    /**
     * 토큰 재발급 - 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰 발급
     */
    @Transactional
    public TokenResponse reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 리프레시 토큰 추출
        String refreshTokenValue = extractTokenFromCookie(request);

        // 쿠키가 없는 경우(이전 버전 호환성) Authorization 헤더 확인
        if (refreshTokenValue == null) {
            refreshTokenValue = extractTokenFromAuthHeader(request);
        }

        if (refreshTokenValue == null) {
            throw new IllegalArgumentException("Refresh 토큰이 존재하지 않습니다.");
        }

        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            // 쿠키 무효화 후 예외 발생
            removeTokenCookies(response);
            throw new IllegalArgumentException("유효하지 않은 Refresh 토큰입니다.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenValue)
            .orElseThrow(() -> {
                removeTokenCookies(response); // 쿠키 무효화
                return new IllegalArgumentException("저장된 Refresh 토큰이 존재하지 않습니다.");
            });

        Member member = memberRepository.findById(refreshToken.getMemberId())
            .orElseThrow(() -> {
                removeTokenCookies(response); // 쿠키 무효화
                return new IllegalArgumentException("회원 정보가 존재하지 않습니다.");
            });

        // 새로운 토큰 발급
        Duration accessTokenDuration = Duration.ofMillis(accessTokenExpiration);
        Duration refreshTokenDuration = Duration.ofMillis(refreshTokenExpiration);

        String newAccessToken = jwtTokenProvider.makeToken(member, accessTokenDuration);
        String newRefreshToken = jwtTokenProvider.makeToken(member, refreshTokenDuration);

        // DB에 새로운 리프레시 토큰 저장
        refreshToken.update(newRefreshToken);
        refreshTokenRepository.save(refreshToken);

        // 쿠키에 새로운 토큰 저장
        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, newAccessToken, accessTokenDuration.toSeconds());
        addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, newRefreshToken, refreshTokenDuration.toSeconds());

        return new TokenResponse(
            newAccessToken,
            newRefreshToken,
            accessTokenExpiration,
            refreshTokenExpiration
        );
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AuthService.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Authorization 헤더에서 토큰 추출 (이전 버전 호환성)
     */
    private String extractTokenFromAuthHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * 쿠키 설정 - ResponseCookie 사용
     */
    private void addTokenCookie(HttpServletResponse response, String name, String value, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .path("/")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .maxAge(maxAge)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 쿠키 무효화 - 만료 처리
     */
    private void removeTokenCookies(HttpServletResponse response) {
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
            .path("/")
            .maxAge(0)
            .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
            .path("/")
            .maxAge(0)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}
