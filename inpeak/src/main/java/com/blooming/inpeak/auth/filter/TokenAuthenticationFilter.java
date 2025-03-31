package com.blooming.inpeak.auth.filter;

import com.blooming.inpeak.auth.repository.RefreshTokenRepository;
import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RefreshTokenRepository refreshTokenRepository;

    private final List<String> REGISTRATION_PATHS = List.of(
        "/api/interest",
        "/api/interest/list",
        "/api/auth/logout",
        "/api/auth/reissue",
        "/api/auth/test"
    );

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException, ServletException {
        String accessToken = getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        String refreshToken = getCookie(request, REFRESH_TOKEN_COOKIE_NAME);

        // accessToken이 없는 경우
        if (accessToken == null) {
            sendErrorResponse(response, "accessToken이 없습니다");
            return;
        }

        // 유효한 토큰(accessToken, refreshToken)에서 userId 추출. (accessToken이 만료되었다면 갱신 시도)
        String userId = getUserIdFromTokens(accessToken, refreshToken, response);
        if (userId == null) {
            return;
        }

        // 3. 사용자 조회
        Member member = findMemberById(userId, response);
        if (member == null) {
            return;
        }

        // 4. 등록 미완료 회원 접근 제한
        if (!isAccessiblePath(request, member)) {
            sendRegistrationErrorResponse(response, "가입이 완료되지 않은 사용자입니다");
            return;
        }

        // 5. 인증 객체 설정 및 다음 필터 실행
        authenticateUser(member);
        filterChain.doFilter(request, response);
    }

    private String getUserIdFromTokens(
        String accessToken, String refreshToken, HttpServletResponse response
    ) throws IOException {
        // 액세스 토큰이 유효한 경우
        if (jwtTokenProvider.validateToken(accessToken)) {
            return jwtTokenProvider.getUserIdFromToken(accessToken);
        }

        // 액세스 토큰 만료, 리프레시 토큰 확인
        log.debug("Access token 만료됨, refresh token으로 갱신 시도");

        // 리프레시 토큰이 없거나 유효하지 않은 경우
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            sendErrorResponse(response, "accessToken이 만료되었고, refreshToken도 유효하지 않습니다");
            return null;
        }

        // 리프레시 토큰을 사용하여 사용자 ID 획득 및 액세스 토큰 갱신
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        reissueAccessToken(userId, response);

        return userId;
    }

    private void reissueAccessToken(String userId, HttpServletResponse response) throws IOException {
        Optional<Member> optionalMember = memberRepository.findById(Long.valueOf(userId));

        if (optionalMember.isEmpty()) {
            sendErrorResponse(response, "refreshToken의 사용자 정보를 찾을 수 없습니다");
            return;
        }

        Member member = optionalMember.get();

        // Access Token 재발급 - 1시간
        String newAccessToken = jwtTokenProvider.makeToken(member, Duration.ofHours(1));

        // Refresh Token 재발급 - 14일
        Duration refreshTokenDuration = Duration.ofDays(14);
        String newRefreshToken = jwtTokenProvider.makeToken(member, refreshTokenDuration);

        updateRefreshTokenInDb(member.getId(), newRefreshToken);

        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, newAccessToken, Duration.ofHours(1).toSeconds());
        addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, newRefreshToken, refreshTokenDuration.toSeconds());
    }

    private void addTokenCookie(HttpServletResponse response, String name, String value, long maxAge) {
        ResponseCookie responseCookie = ResponseCookie.from(name, value)
            .path("/")
            .httpOnly(true)
            .maxAge(maxAge)
            .sameSite("Strict")
            .secure(true)
            .build();
        response.addHeader("Set-Cookie", responseCookie.toString());
    }

    private void updateRefreshTokenInDb(Long memberId, String newRefreshToken) {
        refreshTokenRepository.findByMemberId(memberId)
            .ifPresent(token -> {
                token.update(newRefreshToken);
                refreshTokenRepository.save(token);
            });
    }

    private Member findMemberById(String userId, HttpServletResponse response) throws IOException {
        Optional<Member> optionalMember = memberRepository.findById(Long.valueOf(userId));

        if (optionalMember.isEmpty()) {
            sendErrorResponse(response, "사용자를 찾을 수 없습니다");
            return null;
        }

        return optionalMember.get();
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

    private void authenticateUser(Member member) {
        MemberPrincipal memberPrincipal = MemberPrincipal.create(member, null);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                memberPrincipal, null, memberPrincipal.getAuthorities()
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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
