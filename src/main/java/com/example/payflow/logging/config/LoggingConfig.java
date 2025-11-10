package com.example.payflow.logging.config;

import com.example.payflow.logging.infrastructure.CorrelationIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 로깅 설정
 */
@Configuration
@RequiredArgsConstructor
public class LoggingConfig implements WebMvcConfigurer {
    
    private final CorrelationIdInterceptor correlationIdInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(correlationIdInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/h2-console/**");
    }
}
