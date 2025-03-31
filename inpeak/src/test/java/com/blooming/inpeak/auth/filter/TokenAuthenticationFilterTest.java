package com.blooming.inpeak.auth.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import com.blooming.inpeak.auth.domain.RefreshToken;
import com.blooming.inpeak.auth.repository.RefreshTokenRepository;
import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("TokenAuthenticationFilter 테스트")
@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @InjectMocks
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @DisplayName("액세스 토큰 쿠키가 없는 경우 401 응답을 반환한다")
    @Test
    void whenNoAccessTokenCookie_thenReturnsUnauthorized() throws Exception {
        // given
        given(request.getCookies()).willReturn(null); // 쿠키 없음

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        given(response.getWriter()).willReturn(printWriter);

        // when
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        then(response).should().setStatus(HttpStatus.UNAUTHORIZED.value());
        then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
        then(response).should().setCharacterEncoding("UTF-8");

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assert(responseBody.contains("accessToken이 없습니다"));
    }

    @DisplayName("회원 등록이 완료되지 않은 사용자가 일반 API에 접근하면 488 응답을 반환한다")
    @Test
    void whenUserRegistrationNotCompleted_andAccessRegularAPI_thenReturns488() throws Exception {
        // given
        String accessToken = "valid-access-token";
        String userId = "1";
        Member mockMember = mock(Member.class);

        // 쿠키 설정
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken);
        Cookie[] cookies = new Cookie[] { accessTokenCookie };
        given(request.getCookies()).willReturn(cookies);

        // 일반 API 경로 설정
        given(request.getServletPath()).willReturn("/api/member/profile");

        // 토큰 및 사용자 정보
        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtTokenProvider.getUserIdFromToken(accessToken)).willReturn(userId);
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(mockMember.registrationCompleted()).willReturn(false); // 등록 미완료

        // 응답 캡처 설정
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        given(response.getWriter()).willReturn(writer);

        // when
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        then(response).should().setStatus(488);
        then(filterChain).should(never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        writer.flush();
        assert(stringWriter.toString()).contains("가입이 완료되지 않은 사용자입니다");
    }

    @DisplayName("회원 등록이 완료되지 않은 사용자도 등록 관련 API에는 접근할 수 있다")
    @Test
    void whenUserRegistrationNotCompleted_andAccessRegistrationAPI_thenProceedsToNextFilter() throws Exception {
        // given
        String accessToken = "valid-access-token";
        String userId = "1";
        Member mockMember = mock(Member.class);

        // 쿠키 설정
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken);
        Cookie[] cookies = new Cookie[] { accessTokenCookie };
        given(request.getCookies()).willReturn(cookies);

        // 등록 관련 API 경로 설정
        given(request.getServletPath()).willReturn("/api/interest/list");

        // 토큰 및 사용자 정보
        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtTokenProvider.getUserIdFromToken(accessToken)).willReturn(userId);
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(mockMember.registrationCompleted()).willReturn(false); // 등록 미완료

        // SecurityContext 관련 모킹
        MemberPrincipal mockPrincipal = mock(MemberPrincipal.class);
        try (MockedStatic<MemberPrincipal> mockedStatic = mockStatic(MemberPrincipal.class)) {
            mockedStatic.when(() -> MemberPrincipal.create(any(Member.class), any())).thenReturn(mockPrincipal);

            // when
            tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            then(filterChain).should().doFilter(request, response);
        }
    }
}
