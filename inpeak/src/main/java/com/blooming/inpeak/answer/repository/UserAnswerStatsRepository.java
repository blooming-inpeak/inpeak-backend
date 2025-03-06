package com.blooming.inpeak.answer.repository;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserAnswerStatsRepository {

    private final StringRedisTemplate redisTemplate;

    public void incrementUserAnswerStat(Long memberId, AnswerStatus status) {
        String key = "user:" + memberId;
        redisTemplate.opsForHash().increment(key, status.name(), 1);
    }
}
