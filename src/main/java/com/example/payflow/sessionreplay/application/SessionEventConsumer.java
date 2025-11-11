package com.example.payflow.sessionreplay.application;

import com.example.payflow.sessionreplay.application.dto.InteractionEventDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * 세션 이벤트 Kafka Consumer
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionEventConsumer {
    
    private final EventCollectorService eventCollectorService;
    private final ObjectMapper objectMapper;
    
    /**
     * SessionInteractionEvent 토픽 구독
     */
    @KafkaListener(
        topics = "SessionInteractionEvent",
        groupId = "session-replay-consumer-group",
        containerFactory = "sessionReplayKafkaListenerContainerFactory"
    )
    public void consume(String message, Acknowledgment acknowledgment) {
        try {
            log.debug("Received message from Kafka: {}", message);
            
            InteractionEventDto event = deserializeEvent(message);
            persistEventWithRetry(event);
            
            // 수동 오프셋 커밋
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
            log.debug("Event processed successfully: sessionId={}", event.getSessionId());
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize event: {}", message, e);
            // 역직렬화 실패는 재시도하지 않고 다음 메시지로 이동
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            log.error("Failed to process event after retries: {}", message, e);
            // 재시도 실패 후에도 오프셋 커밋하여 무한 재시도 방지
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        }
    }
    
    /**
     * 이벤트 역직렬화
     */
    private InteractionEventDto deserializeEvent(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, InteractionEventDto.class);
    }
    
    /**
     * 이벤트 저장 (재시도 포함)
     */
    @Retryable(
        value = {DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void persistEventWithRetry(InteractionEventDto event) {
        eventCollectorService.persistEvent(event);
    }
    
    /**
     * 재시도 실패 시 복구 메서드
     */
    @Recover
    public void recover(DataAccessException e, InteractionEventDto event) {
        log.error("Failed to persist event after {} retries: sessionId={}, eventType={}", 
            3, event.getSessionId(), event.getEventType(), e);
        // Dead Letter Queue로 전송하거나 별도 에러 로그 저장 가능
    }
}
