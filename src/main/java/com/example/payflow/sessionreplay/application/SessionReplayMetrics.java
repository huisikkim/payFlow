package com.example.payflow.sessionreplay.application;

import com.example.payflow.logging.application.EventLoggingService;
import com.example.payflow.sessionreplay.domain.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * 세션 재생 메트릭 수집
 * 기존 EventLoggingService와 통합
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionReplayMetrics {
    
    private final EventLoggingService eventLoggingService;
    
    /**
     * 이벤트 수집 메트릭 로깅
     */
    public void logEventCollected(String sessionId, EventType eventType) {
        try {
            eventLoggingService.logEvent(
                UUID.randomUUID().toString(),
                "SessionEventCollected",
                "session-replay",
                Map.of(
                    "sessionId", sessionId,
                    "eventType", eventType.name()
                )
            );
        } catch (Exception e) {
            log.error("Failed to log event collection metric", e);
        }
    }
    
    /**
     * 이벤트 처리 메트릭 로깅
     */
    public void logEventProcessed(String sessionId, EventType eventType, long processingTimeMs) {
        try {
            eventLoggingService.logEvent(
                UUID.randomUUID().toString(),
                "SessionEventProcessed",
                "session-replay",
                Map.of(
                    "sessionId", sessionId,
                    "eventType", eventType.name(),
                    "processingTimeMs", processingTimeMs
                )
            );
        } catch (Exception e) {
            log.error("Failed to log event processing metric", e);
        }
    }
    
    /**
     * Consumer 지연 메트릭 로깅
     */
    public void logConsumerLag(long lagMs) {
        try {
            eventLoggingService.logEvent(
                UUID.randomUUID().toString(),
                "SessionReplayConsumerLag",
                "session-replay",
                Map.of("lagMs", lagMs)
            );
        } catch (Exception e) {
            log.error("Failed to log consumer lag metric", e);
        }
    }
    
    /**
     * 세션 생성 메트릭 로깅
     */
    public void logSessionCreated(String sessionId, String userId) {
        try {
            eventLoggingService.logEvent(
                UUID.randomUUID().toString(),
                "SessionCreated",
                "session-replay",
                Map.of(
                    "sessionId", sessionId,
                    "userId", userId
                )
            );
        } catch (Exception e) {
            log.error("Failed to log session creation metric", e);
        }
    }
    
    /**
     * 세션 재생 메트릭 로깅
     */
    public void logSessionReplayed(String sessionId, String adminUserId) {
        try {
            eventLoggingService.logEvent(
                UUID.randomUUID().toString(),
                "SessionReplayed",
                "session-replay",
                Map.of(
                    "sessionId", sessionId,
                    "adminUserId", adminUserId
                )
            );
        } catch (Exception e) {
            log.error("Failed to log session replay metric", e);
        }
    }
}
