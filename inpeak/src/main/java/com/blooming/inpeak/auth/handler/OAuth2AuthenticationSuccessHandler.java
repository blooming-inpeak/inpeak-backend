package com.blooming.inpeak.auth.handler;

import com.blooming.inpeak.auth.domain.RefreshToken;
import com.blooming.inpeak.auth.repository.RefreshTokenRepository;
import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${jwt.redirectUri}")
    private String redirectUrl;

    @Value("${jwt.accessToken.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refreshToken.expiration}")
    private long refreshTokenExpiration;

    // 쿠키 관련 상수 추가
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        MemberPrincipal oAuth2User = (MemberPrincipal) authentication.getPrincipal();
        Member member = memberRepository.findByKakaoId(oAuth2User.kakaoId())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Duration accessTokenDuration = Duration.ofMillis(accessTokenExpiration);
        Duration refreshTokenDuration = Duration.ofMillis(refreshTokenExpiration);

        String accessToken = jwtTokenProvider.makeToken(member, accessTokenDuration);
        String refreshToken = jwtTokenProvider.makeToken(member, refreshTokenDuration);

        storeOrGenerate(member, refreshToken);

        // 토큰을 쿠키에 저장
        addTokenCookie(
            response, ACCESS_TOKEN_COOKIE_NAME, accessToken, (int) accessTokenDuration.toSeconds()
        );
        addTokenCookie(
            response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, (int) refreshTokenDuration.toSeconds()
        );

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
//        cookie.setSecure(true); // HTTPS 환경에서만 활성화
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void storeOrGenerate(Member member, String refreshToken) {
        RefreshToken refreshTokenInstance = refreshTokenRepository.findByMemberId(member.getId())
            .orElse(
                RefreshToken.builder()
                    .memberId(member.getId())
                    .refreshToken(refreshToken)
                    .build()
            );
        refreshTokenInstance.update(refreshToken);
        refreshTokenRepository.save(refreshTokenInstance);
    }
}
