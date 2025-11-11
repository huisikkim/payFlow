package com.example.payflow.sessionreplay.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Rate Limiting 설정
 */
@Configuration
public class RateLimitConfig {
    
    @Value("${session-replay.rate-limit:1000}")
    private double rateLimit;
    
    @Bean
    public RateLimiter eventCollectorRateLimiter() {
        return RateLimiter.create(rateLimit); // requests per second
    }
}
