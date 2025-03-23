package com.blooming.inpeak.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "https://inpeak-frontend.vercel.app",
                        "https://inpeak.kr",
                        "https://www.inpeak.kr"
                )
                .exposedHeaders(HttpHeaders.LOCATION)
                .allowCredentials(true);
    }
}
