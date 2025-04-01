package com.blooming.inpeak.auth.filter;

import com.blooming.inpeak.auth.domain.RefreshToken;
import com.blooming.inpeak.auth.repository.RefreshTokenRepository;
import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.auth.utils.TokenExtractor;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final TokenExtractor tokenExtractor;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.accessToken.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refreshToken.expiration}")
    private long refreshTokenExpiration;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final List<String> REGISTRATION_PATHS = List.of(
        "/api/interest",
        "/api/interest/list",
        "/api/auth/logout",
        "/api/auth/reissue",
        "/api/auth/test"
    );

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException {
        String accessToken = tokenExtractor.extractAccessToken(request);

        // accessToken이 없는 경우
        if (accessToken == null) {
            sendErrorResponse(response, "accessToken이 없습니다");
            return;
        }

        try {
            // 토큰이 유효한 경우
            if (jwtTokenProvider.validateToken(accessToken)) {
                String userId = jwtTokenProvider.getUserIdFromToken(accessToken);
                authenticateUser(userId, request, response);
                filterChain.doFilter(request, response);
                return;
            }

            // accessToken이 만료된 경우 - refreshToken 확인
            log.debug("Access token 만료됨, refresh token으로 갱신 시도");
            String refreshToken = tokenExtractor.extractRefreshToken(request);

            if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
                sendErrorResponse(response, "accessToken이 만료되었고, refreshToken도 유효하지 않습니다");
                return;
            }

            // refreshToken이 유효한 경우, 새 토큰 발급
            String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

            // DB에 저장된 토큰과 일치하는지 확인
            RefreshToken savedRefreshToken = refreshTokenRepository.findByMemberId(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("저장된 Refresh 토큰이 존재하지 않습니다."));

            if (!savedRefreshToken.getRefreshToken().equals(refreshToken)) {
                removeTokenCookies(response);
                sendErrorResponse(response, "유효하지 않은 Refresh 토큰입니다.");
                return;
            }

            // 토큰 재발급 및 쿠키 설정
            Member member = memberRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 존재하지 않습니다."));

            // 새 토큰 발급
            String newAccessToken = reissueAccessToken(member, savedRefreshToken, response);

            // SecurityContext 설정
            authenticateUser(userId, request, response);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            sendErrorResponse(response, "인증 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String reissueAccessToken(Member member, RefreshToken refreshToken, HttpServletResponse response) {
        // 새로운 토큰 발급
        Duration accessTokenDuration = Duration.ofMillis(accessTokenExpiration);
        String newAccessToken = jwtTokenProvider.makeToken(member, accessTokenDuration);

        // 필요한 경우에만 refreshToken도 재발급 (예: 유효기간이 절반 이하로 남은 경우)
        long currentTimeMillis = System.currentTimeMillis();
        if (jwtTokenProvider.getExpirationTime(refreshToken.getRefreshToken()) - currentTimeMillis < refreshTokenExpiration / 2) {
            Duration refreshTokenDuration = Duration.ofMillis(refreshTokenExpiration);
            String newRefreshToken = jwtTokenProvider.makeToken(member, refreshTokenDuration);
            refreshToken.update(newRefreshToken);
            refreshTokenRepository.save(refreshToken);

            // 쿠키에 새 refreshToken 설정
            addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, newRefreshToken, refreshTokenDuration.toSeconds());
        }

        // 쿠키에 새 accessToken 설정
        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, newAccessToken, accessTokenDuration.toSeconds());

        return newAccessToken;
    }

    private void authenticateUser(String userId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Member member = memberRepository.findById(Long.valueOf(userId))
            .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));

        // 등록 완료 여부 확인 및 접근 제한
        if (!isAccessiblePath(request, member)) {
            sendRegistrationErrorResponse(response, "가입이 완료되지 않은 사용자입니다");
            return;
        }

        MemberPrincipal memberPrincipal = MemberPrincipal.create(member, null);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                memberPrincipal, null, memberPrincipal.getAuthorities()
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isAccessiblePath(HttpServletRequest request, Member member) {
        // 회원이 등록 완료 상태라면 모든 경로 접근 가능
        if (member.registrationCompleted()) {
            return true;
        }

        // 등록 미완료 회원은 허용된 경로만 접근 가능
        String path = request.getServletPath();
        return REGISTRATION_PATHS.stream()
            .anyMatch(path::startsWith);
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

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
        log.error("인증 실패: {}", message);
    }

    private void sendRegistrationErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(488);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
        log.error("가입 미완료 사용자 차단: {}", message);
    }
}
