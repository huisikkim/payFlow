package com.example.payflow.logging.application;

import com.example.payflow.logging.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이벤트 로깅 서비스
 * 모든 비즈니스 이벤트를 중앙 집중식으로 수집하고 저장
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventLoggingService {
    
    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 이벤트 로그 생성
     */
    @Transactional
    public EventLog logEvent(EventLogRequest request) {
        EventLog eventLog = EventLog.builder()
            .correlationId(request.getCorrelationId() != null ? 
                request.getCorrelationId() : UUID.randomUUID().toString())
            .eventType(request.getEventType())
            .serviceName(request.getServiceName())
            .payload(serializePayload(request.getPayload()))
            .timestamp(LocalDateTime.now())
            .userId(request.getUserId())
            .status(request.getStatus() != null ? request.getStatus() : EventStatus.SUCCESS)
            .errorMessage(request.getErrorMessage())
            .processingTimeMs(request.getProcessingTimeMs())
            .build();
        
        EventLog saved = eventLogRepository.save(eventLog);
        log.info("Event logged: {} - {} [{}]", 
            saved.getEventType(), saved.getCorrelationId(), saved.getStatus());
        
        return saved;
    }
    
    /**
     * 이벤트 로그 생성 (간편 버전)
     */
    @Transactional
    public EventLog logEvent(String correlationId, String eventType, 
                            String serviceName, Object payload) {
        return logEvent(EventLogRequest.builder()
            .correlationId(correlationId)
            .eventType(eventType)
            .serviceName(serviceName)
            .payload(payload)
            .status(EventStatus.SUCCESS)
            .build());
    }
    
    /**
     * 실패 이벤트 로그
     */
    @Transactional
    public EventLog logFailedEvent(String correlationId, String eventType, 
                                   String serviceName, String errorMessage) {
        return logEvent(EventLogRequest.builder()
            .correlationId(correlationId)
            .eventType(eventType)
            .serviceName(serviceName)
            .status(EventStatus.FAILED)
            .errorMessage(errorMessage)
            .build());
    }
    
    private String serializePayload(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Failed to serialize payload", e);
            return payload.toString();
        }
    }
}
