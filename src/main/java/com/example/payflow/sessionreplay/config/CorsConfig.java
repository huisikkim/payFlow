package com.example.payflow.sessionreplay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Session Replay CORS 설정
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${session-replay.cors.allowed-origins:http://localhost:8080}")
    private String[] allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/session-replay/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("POST", "GET", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }
}
