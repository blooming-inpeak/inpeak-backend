package com.blooming.inpeak.answer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;


class UserAnswerStatsRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private UserAnswerStatsRepository userAnswerStatsRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("사용자 정답 통계 증가 테스트")
    void incrementUserAnswerStat_ShouldStoreValueInRedis() {
        // ✅ Given: 테스트할 사용자 ID와 상태
        Long memberId = 1L;
        AnswerStatus status = AnswerStatus.CORRECT;
        String key = "user:" + memberId;

        // ✅ When: 정답 카운트 증가
        userAnswerStatsRepository.incrementUserAnswerStat(memberId, status);

        // ✅ Then: Redis에서 값이 정상적으로 증가했는지 검증
        String storedValue = redisTemplate.opsForHash().get(key, status.name()).toString();
        assertThat(Integer.parseInt(storedValue)).isEqualTo(1);
    }

    @Test
    @DisplayName("사용자 정답 통계 증가 테스트 - 기존 값 증가")
    void incrementUserAnswerStat_ShouldIncreaseExistingValue() {
        // ✅ Given: 이미 1회 증가된 상태
        Long memberId = 2L;
        AnswerStatus status = AnswerStatus.INCORRECT;
        String key = "user:" + memberId;

        // ✅ When: 두 번 증가
        userAnswerStatsRepository.incrementUserAnswerStat(memberId, status);
        userAnswerStatsRepository.incrementUserAnswerStat(memberId, status);

        // ✅ Then: 2회 증가되었는지 검증
        String storedValue = redisTemplate.opsForHash().get(key, status.name()).toString();
        assertThat(Integer.parseInt(storedValue)).isEqualTo(2);
    }
}
