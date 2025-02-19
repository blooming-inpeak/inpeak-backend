package com.blooming.inpeak.auth.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.blooming.inpeak.auth.domain.RefreshToken;
import com.blooming.inpeak.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("RefreshTokenRepository 테스트")
class RefreshTokenRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @DisplayName("memberId로 RefreshToken 조회")
    @Test
    void findByMemberId() {
        // given
        Long memberId = 1L;
        RefreshToken token = RefreshToken.builder()
            .memberId(memberId)
            .refreshToken("test-token")
            .build();
        refreshTokenRepository.save(token);

        // when
        RefreshToken found = refreshTokenRepository.findByMemberId(memberId)
            .orElseThrow(() -> new IllegalStateException("해당 memberId의 RefreshToken이 존재하지 않습니다."));

        // then
        assertThat(found.getRefreshToken()).isEqualTo("test-token");
    }

    @DisplayName("refreshToken 값으로 엔티티 조회 성공")
    @Test
    void findByRefreshToken() {
        // given
        String tokenValue = "unique-token-value";
        RefreshToken refreshToken = RefreshToken.builder()
            .memberId(1L)
            .refreshToken(tokenValue)
            .build();
        refreshTokenRepository.save(refreshToken);

        // when
        RefreshToken found = refreshTokenRepository.findByRefreshToken(tokenValue)
            .orElseThrow(() -> new IllegalStateException("해당 refreshToken의 RefreshToken이 존재하지 않습니다."));

        // then
        assertThat(found.getRefreshToken()).isEqualTo(tokenValue);
    }
}
