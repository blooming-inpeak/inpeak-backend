package com.blooming.inpeak.support;

import com.blooming.inpeak.TestRedisConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfiguration.class)
public abstract class IntegrationTestSupport {
}
