package com.blooming.inpeak.auth.service;

import com.blooming.inpeak.auth.domain.RefreshToken;
import com.blooming.inpeak.auth.dto.TokenResponse;
import com.blooming.inpeak.auth.repository.RefreshTokenRepository;
import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    @Transactional
    public TokenResponse reissueToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String refreshTokenValue = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            refreshTokenValue = authorizationHeader.substring(7);
        } else {
            refreshTokenValue = extractRefreshTokenFromCookie(request);
        }

        if (refreshTokenValue == null) {
            throw new IllegalArgumentException("Refresh 토큰이 존재하지 않습니다.");
        }

        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh 토큰입니다.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenValue)
            .orElseThrow(() -> new IllegalArgumentException("저장된 Refresh 토큰이 존재하지 않습니다."));

        Member member = memberRepository.findById(refreshToken.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("회원 정보가 존재하지 않습니다."));

        Duration accessTokenDuration = Duration.ofMillis(accessTokenExpiration);
        Duration refreshTokenDuration = Duration.ofMillis(refreshTokenExpiration);

        String newAccessToken = jwtTokenProvider.makeToken(member, accessTokenDuration);
        String newRefreshToken = jwtTokenProvider.makeToken(member, refreshTokenDuration);

        refreshToken.update(newRefreshToken);
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(
            newAccessToken,
            newRefreshToken,
            accessTokenExpiration,
            refreshTokenExpiration
        );
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
