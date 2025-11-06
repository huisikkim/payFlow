package com.example.payflow.common.infrastructure;

import com.example.payflow.common.event.DomainEvent;
import com.example.payflow.common.event.EventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public void publish(DomainEvent event) {
        try {
            String topic = event.getEventType();
            String message = objectMapper.writeValueAsString(event);
            
            kafkaTemplate.send(topic, event.getEventId(), message);
            log.info("📨 이벤트 발행: topic={}, eventId={}", topic, event.getEventId());
        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패", e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }
}
