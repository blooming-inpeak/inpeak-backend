package com.blooming.inpeak.common.config.AI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GPTConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate template = new RestTemplate();
        template.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add(
                "Authorization"
                , "Bearer " + apiKey);
            return execution.execute(request, body);
        });

        return template;

    }

    @Bean("whisperRestTemplate")
    public RestTemplate whisperRestTemplate() {
        return new RestTemplate(); // 헤더 인터셉터 없는 깨끗한 RestTemplate
    }
}
