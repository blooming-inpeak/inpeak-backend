package com.blooming.inpeak.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.blooming.inpeak.auth.utils.JwtTokenProvider;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import com.blooming.inpeak.member.domain.RegistrationStatus;
import com.blooming.inpeak.support.IntegrationTestSupport;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("토큰 생성 및 검증 테스트")
    @Nested
    class TokenGenerationAndValidation {

        @DisplayName("토큰에서 사용자 ID 추출 성공")
        @Test
        void getUserIdFromToken_shouldReturnCorrectId() {
            // given
            Long memberId = 1L;
            Member testMember = createTestMember(memberId);
            String token = jwtTokenProvider.makeToken(testMember, Duration.ofMinutes(30));

            // when
            String extractedId = jwtTokenProvider.getUserIdFromToken(token);

            // then
            assertThat(extractedId).isEqualTo(memberId.toString());
        }

        @DisplayName("유효한 토큰 검증 성공")
        @Test
        void validateToken_withValidToken_shouldReturnTrue() {
            // given
            Member testMember = createTestMember(1L);
            String token = jwtTokenProvider.makeToken(testMember, Duration.ofMinutes(30));

            // when
            boolean isValid = jwtTokenProvider.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @DisplayName("만료된 토큰 검증 실패")
        @Test
        void validateToken_withExpiredToken_shouldReturnFalse() {
            // given
            Member testMember = createTestMember(1L);
            String token = jwtTokenProvider.makeToken(testMember, Duration.ofMillis(1));

            // 토큰 만료 대기
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // when
            boolean isValid = jwtTokenProvider.validateToken(token);

            // then
            assertThat(isValid).isFalse();
        }

        @DisplayName("잘못된 서명의 토큰 검증 실패")
        @Test
        void validateToken_withInvalidSignature_shouldReturnFalse() {
            // given
            Member testMember = createTestMember(1L);
            String token = jwtTokenProvider.makeToken(testMember, Duration.ofMinutes(30));
            String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "tampered";

            // when
            boolean isValid = jwtTokenProvider.validateToken(tampered);

            // then
            assertThat(isValid).isFalse();
        }
    }

    @DisplayName("예외 케이스 테스트")
    @Nested
    class ExceptionCases {

        @DisplayName("만료된 토큰에서 ID 추출 시 예외 발생")
        @Test
        void getUserIdFromToken_withExpiredToken_shouldThrowException() {
            // given
            Member testMember = createTestMember(1L);
            String expiredToken = jwtTokenProvider.makeToken(testMember, Duration.ofMillis(1));

            // 토큰 만료 대기
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // when & then
            assertThatThrownBy(
                () -> jwtTokenProvider.getUserIdFromToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
        }

        @DisplayName("변조된 토큰에서 ID 추출 시 예외 발생")
        @Test
        void getUserIdFromToken_withTamperedToken_shouldThrowException() {
            // given
            Member testMember = createTestMember(1L);
            String token = jwtTokenProvider.makeToken(testMember, Duration.ofMinutes(30));
            String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "tampered";

            // when & then
            assertThatThrownBy(
                () -> jwtTokenProvider.getUserIdFromToken(tampered))
                .isInstanceOf(SignatureException.class);
        }
    }

    /**
     * 테스트용 Member 객체 생성 헬퍼 메서드
     */
    private Member createTestMember(Long id) {
        return Member.builder()
            .id(id)
            .kakaoId(1234567890L)
            .nickname("테스트유저")
            .provider(OAuth2Provider.KAKAO)
            .registrationStatus(RegistrationStatus.COMPLETED)
            .build();
    }
}
