package com.blooming.inpeak.auth.filter;

import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException, ServletException {

        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

        // 토큰이 없거나 형식이 맞지 않을 경우
        if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
            sendErrorResponse(response, "헤더에 액세스 토큰이 없거나, Bearer 접두어가 빠져있습니다");
            return;
        }

        String token = authorizationHeader.substring(TOKEN_PREFIX.length());

        // 토큰 검증 실패 시
        if (!jwtTokenProvider.validateToken(token)) {
            sendErrorResponse(response, "유효하지 않은 토큰입니다");
            return;
        }

        // 토큰이 유효한 경우, 사용자 정보 설정 추가 필요
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        Optional<Member> optionalMember = memberRepository.findById(Long.valueOf(userId));

        if (optionalMember.isEmpty()) {
            sendErrorResponse(response, "사용자를 찾을 수 없습니다");
            return;
        }

        Member member = optionalMember.get();

        if (!member.registrationCompleted()) {
            sendRegistrationErrorResponse(response, "가입이 완료되지 않은 사용자입니다");
            return;
        }

        // 인증 객체 생성 및 설정
        MemberPrincipal memberPrincipal = MemberPrincipal.create(member, null); // 속성은 필요에 따라 조정
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                memberPrincipal, null, memberPrincipal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");

        if (log.isErrorEnabled()) {
            log.error("인증 필터에서 요청이 중단됨: {}", message);
        }
    }

    private void sendRegistrationErrorResponse(
        HttpServletResponse response, String message
    ) throws IOException {
        response.setStatus(488);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");

        if (log.isErrorEnabled()) {
            log.error("가입이 완료되지 않은 사용자입니다: {}", message);
        }
    }
}
