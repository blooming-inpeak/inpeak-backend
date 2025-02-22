package com.blooming.inpeak.auth.filter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @DisplayName("유효한 토큰인 경우 다음 필터로 진행된다")
    @Test
    void whenValidToken_thenProceedsToNextFilter() throws Exception {
        // given
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);

        // when
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }

    @DisplayName("Authorization 헤더가 없는 경우 401 응답을 반환한다")
    @Test
    void whenNoAuthorizationHeader_thenReturnsUnauthorized() throws Exception {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // when
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assert(responseBody.contains("헤더에 액세스 토큰이 없거나, Bearer 접두어가 빠져있습니다"));
    }
}
