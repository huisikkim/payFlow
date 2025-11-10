package com.example.payflow.logging.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Correlation ID 인터셉터
 * 모든 HTTP 요청에 고유한 Correlation ID를 부여하여 분산 추적 가능
 */
@Component
@Slf4j
public class CorrelationIdInterceptor implements HandlerInterceptor {
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 헤더에서 Correlation ID 추출 또는 생성
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // MDC에 저장 (로그에 자동으로 포함됨)
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        // 응답 헤더에도 추가
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        log.debug("Correlation ID: {}", correlationId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // MDC 정리
        MDC.remove(CORRELATION_ID_MDC_KEY);
    }
    
    /**
     * 현재 스레드의 Correlation ID 조회
     */
    public static String getCurrentCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}
