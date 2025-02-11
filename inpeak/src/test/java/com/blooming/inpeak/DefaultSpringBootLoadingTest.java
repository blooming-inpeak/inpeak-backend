package com.blooming.inpeak;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class DefaultSpringBootLoadingTest extends IntegrationTestSupport {

    private static final String TEST_KEY = "test:key";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void cleanup() {
        redisTemplate.delete(TEST_KEY);
    }

    @DisplayName("스프링 부트 로딩 테스트 및 Redis 연결 테스트")
    @Test
    void testPropertyLoad() {
        // given
        String value = "test:value";

        // when
        redisTemplate.opsForValue().set(TEST_KEY, value);
        String retrievedValue = redisTemplate.opsForValue().get(TEST_KEY);

        // then
        assertThat(retrievedValue).isEqualTo(value);
    }
}
