package com.blooming.inpeak.auth.filter;

import com.blooming.inpeak.auth.domain.RefreshToken;
import com.blooming.inpeak.auth.repository.RefreshTokenRepository;
import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.auth.utils.TokenExtractor;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

    private static final List<String> ALLOWED_UNREGISTERED_PATHS = List.of(
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
    ) throws IOException, ServletException {
        String accessToken = extractAndValidateAccessToken(request, response);
        if (accessToken == null) return;

        String memberId = jwtTokenProvider.getUserIdFromToken(accessToken);
        if (!isMemberAllowedToAccess(request, memberId)) {
            sendRegistrationErrorResponse(response);
            return;
        }

        authenticateMember(memberId);
        filterChain.doFilter(request, response);
    }

    private String extractAndValidateAccessToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        String accessToken = tokenExtractor.extractAccessToken(request);

        // 액세스 토큰 없음
        if (accessToken == null) {
            sendErrorResponse(response, "액세스 토큰이 없습니다");
            return null;
        }

        // 액세스 토큰 유효한 경우 바로 반환
        if (jwtTokenProvider.validateToken(accessToken)) {
            return accessToken;
        }

        // 액세스 토큰 만료된 경우 리프레시 토큰으로 갱신 시도
        return handleExpiredAccessToken(request, response);
    }

    private String handleExpiredAccessToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        log.debug("액세스 토큰 만료됨, 리프레시 토큰으로 갱신 시도");

        String refreshToken = tokenExtractor.extractRefreshToken(request);

        // 리프레시 토큰 유효성 검사
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            sendErrorResponse(response, "액세스 토큰이 만료되었고, 리프레시 토큰도 유효하지 않습니다");
            return null;
        }

        String memberId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // DB에 저장된 리프레시 토큰 검증
        RefreshToken savedRefreshToken = refreshTokenRepository.findByMemberId(Long.valueOf(memberId))
            .orElseThrow(() -> new IllegalArgumentException("저장된 리프레시 토큰이 존재하지 않습니다."));

        if (!savedRefreshToken.getRefreshToken().equals(refreshToken)) {
            removeTokenCookies(response);
            sendErrorResponse(response, "유효하지 않은 리프레시 토큰입니다.");
            return null;
        }

        // 새 토큰 발급
        Member member = memberRepository.findById(Long.valueOf(memberId))
            .orElseThrow(() -> new IllegalArgumentException("회원 정보가 존재하지 않습니다."));

        return reissueAccessToken(member, savedRefreshToken, response);
    }

    private boolean isMemberAllowedToAccess(HttpServletRequest request, String memberId) {
        Member member = memberRepository.findById(Long.valueOf(memberId))
            .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));

        // 회원 등록 완료 상태라면 모든 경로 접근 가능
        if (member.registrationCompleted()) {
            return true;
        }

        // 등록 미완료 회원은 허용된 경로만 접근 가능
        String path = request.getServletPath();
        return ALLOWED_UNREGISTERED_PATHS.stream()
            .anyMatch(path::startsWith);
    }

    private void authenticateMember(String memberId) {
        Member member = memberRepository.findById(Long.valueOf(memberId))
            .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));

        MemberPrincipal memberPrincipal = MemberPrincipal.create(member, null);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                memberPrincipal, null, memberPrincipal.getAuthorities()
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String reissueAccessToken(
        Member member, RefreshToken refreshToken, HttpServletResponse response) {
        // 새로운 액세스 토큰 발급
        Duration accessTokenDuration = Duration.ofMillis(accessTokenExpiration);
        String newAccessToken = jwtTokenProvider.makeToken(member, accessTokenDuration);

        // 리프레시 토큰 재발급 (유효 기간 절반 이하 남은 경우)
        var currentTimeMillis = System.currentTimeMillis();
        var expirationTime = jwtTokenProvider.getExpirationTime(refreshToken.getRefreshToken());
        if (expirationTime - currentTimeMillis < refreshTokenExpiration / 2) {
            Duration refreshTokenDuration = Duration.ofMillis(refreshTokenExpiration);
            String newRefreshToken = jwtTokenProvider.makeToken(member, refreshTokenDuration);
            refreshToken.update(newRefreshToken);
            refreshTokenRepository.save(refreshToken);

            // 쿠키에 새 리프레시 토큰 설정
            addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, newRefreshToken, refreshTokenDuration.toSeconds());
        }

        // 쿠키에 새 accessToken 설정
        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, newAccessToken, accessTokenDuration.toSeconds());

        return newAccessToken;
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

    private void sendRegistrationErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(488);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"가입이 완료되지 않은 사용자입니다\"}");
        log.error("가입 미완료 사용자 차단");
    }
}
