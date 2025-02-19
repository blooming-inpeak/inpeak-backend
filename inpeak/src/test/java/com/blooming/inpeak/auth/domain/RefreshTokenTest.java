package com.blooming.inpeak.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RefreshToken 도메인 테스트")
class RefreshTokenTest {

    @DisplayName("RefreshToken 생성")
    @Test
    void createRefreshToken() {
        // given
        Long memberId = 1L;
        String token = "test-refresh-token";

        // when
        RefreshToken refreshToken = RefreshToken.builder()
            .memberId(memberId)
            .refreshToken(token)
            .build();

        // then
        assertThat(refreshToken.getMemberId()).isEqualTo(memberId);
        assertThat(refreshToken.getRefreshToken()).isEqualTo(token);
    }

    @DisplayName("RefreshToken 업데이트")
    @Test
    void updateRefreshToken() {
        // given
        Long memberId = 1L;
        RefreshToken refreshToken = RefreshToken.builder()
            .memberId(memberId)
            .refreshToken("old-refresh-token")
            .build();

        String newToken = "new-refresh-token";

        // when
        refreshToken.update(newToken);

        // then
        assertThat(refreshToken.getRefreshToken()).isEqualTo(newToken);
    }
}
