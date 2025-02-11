package com.blooming.inpeak;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;
import redis.embedded.core.RedisServerBuilder;

@TestConfiguration
public class TestRedisConfiguration {

    private final RedisServer redisServer;

    public TestRedisConfiguration(
        @Value("${spring.data.redis.port}") int redisPort) throws IOException {
        this.redisServer = new RedisServerBuilder()
            .port(redisPort)
            .setting("maxmemory 128M")
            .setting("maxmemory-policy allkeys-lru")
            .build();
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        redisServer.stop();
    }
}
