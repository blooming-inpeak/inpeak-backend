package com.blooming.inpeak.auth.handler;

import com.blooming.inpeak.auth.domain.RefreshToken;
import com.blooming.inpeak.auth.repository.RefreshTokenRepository;
import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static String REDIRECT_URL = "http://localhost:8080/";
    public static Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(30);
    public static Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);

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

        String accessToken = jwtTokenProvider.makeToken(member, ACCESS_TOKEN_DURATION);
        String refreshToken = jwtTokenProvider.makeToken(member, REFRESH_TOKEN_DURATION);

        storeOrGenerate(member, refreshToken);

        //리다이렉트 URL 생성, 차후에 쿠키 방식으로 리펙토링
        String redirectUrl = ServletUriComponentsBuilder
            .fromUriString(REDIRECT_URL)
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build()
            .toUriString();

        getRedirectStrategy()
            .sendRedirect(request, response, redirectUrl);
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
