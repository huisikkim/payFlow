package com.example.payflow.sessionreplay.application;

import com.example.payflow.sessionreplay.application.dto.InteractionEventDto;
import com.example.payflow.sessionreplay.domain.InteractionEvent;
import com.example.payflow.sessionreplay.domain.InteractionEventRepository;
import com.example.payflow.sessionreplay.domain.Session;
import com.example.payflow.sessionreplay.domain.SessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이벤트 수집 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventCollectorService {
    
    private final KafkaTemplate<String, String> sessionReplayKafkaTemplate;
    private final SessionRepository sessionRepository;
    private final InteractionEventRepository interactionEventRepository;
    private final ObjectMapper objectMapper;
    private final com.example.payflow.sessionreplay.infrastructure.PayloadEncryptor payloadEncryptor;
    private final SessionReplayMetrics metrics;
    
    /**
     * 이벤트 배치를 Kafka로 발행
     */
    public void publishEvents(List<InteractionEventDto> events) {
        for (InteractionEventDto event : events) {
            publishEvent(event);
        }
    }
    
    /**
     * 단일 이벤트를 Kafka로 발행
     */
    public void publishEvent(InteractionEventDto event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            sessionReplayKafkaTemplate.send("SessionInteractionEvent", event.getSessionId(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event: sessionId={}", event.getSessionId(), ex);
                    } else {
                        log.debug("Event published: sessionId={}, eventType={}", 
                            event.getSessionId(), event.getEventType());
                    }
                });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event", e);
            throw new EventSerializationException("Event serialization failed", e);
        }
    }
    
    /**
     * 이벤트를 데이터베이스에 저장
     */
    @Transactional
    public void persistEvent(InteractionEventDto eventDto) {
        long startTime = System.currentTimeMillis();
        
        // 세션 조회 또는 생성
        Session session = sessionRepository.findById(eventDto.getSessionId())
            .orElseGet(() -> createNewSession(eventDto.getSessionId()));
        
        // 이벤트 저장 (암호화)
        String payloadJson = serializePayload(eventDto.getPayload());
        String encryptedPayload = payloadEncryptor.encrypt(payloadJson);
        InteractionEvent event = InteractionEvent.builder()
            .sessionId(eventDto.getSessionId())
            .eventType(eventDto.getEventType())
            .timestamp(eventDto.getTimestamp())
            .payload(encryptedPayload)
            .createdAt(LocalDateTime.now())
            .build();
        
        interactionEventRepository.save(event);
        
        // 세션 메타데이터 업데이트
        session.addEvent();
        sessionRepository.save(session);
        
        // 메트릭 로깅
        long processingTime = System.currentTimeMillis() - startTime;
        metrics.logEventProcessed(eventDto.getSessionId(), eventDto.getEventType(), processingTime);
        
        log.debug("Event persisted: sessionId={}, eventType={}", 
            eventDto.getSessionId(), eventDto.getEventType());
    }
    
    /**
     * 새 세션 생성
     */
    private Session createNewSession(String sessionId) {
        String userId = "anonymous"; // 추후 Security Context에서 가져올 수 있음
        
        Session session = Session.builder()
            .sessionId(sessionId)
            .userId(userId)
            .startTime(LocalDateTime.now())
            .totalEvents(0)
            .createdAt(LocalDateTime.now())
            .build();
        
        Session saved = sessionRepository.save(session);
        
        // 메트릭 로깅
        metrics.logSessionCreated(sessionId, userId);
        
        return saved;
    }
    
    /**
     * Payload를 JSON 문자열로 직렬화
     */
    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload", e);
            return "{}";
        }
    }
}
