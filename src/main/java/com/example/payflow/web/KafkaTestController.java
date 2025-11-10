package com.example.payflow.web;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.common.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class KafkaTestController {
    
    private final EventPublisher eventPublisher;
    
    @PostMapping("/kafka")
    public String testKafka(@RequestParam(defaultValue = "TestEvent") String eventType) {
        TestEvent event = new TestEvent(
            UUID.randomUUID().toString(),
            eventType,
            LocalDateTime.now(),
            "테스트 메시지: " + System.currentTimeMillis()
        );
        
        eventPublisher.publish(event);
        
        return "이벤트 발행 완료: " + eventType + " (eventId: " + event.getEventId() + ")";
    }
    
    static class TestEvent implements DomainEvent {
        private final String eventId;
        private final String eventType;
        private final LocalDateTime occurredOn;
        private final String message;
        
        public TestEvent(String eventId, String eventType, LocalDateTime occurredOn, String message) {
            this.eventId = eventId;
            this.eventType = eventType;
            this.occurredOn = occurredOn;
            this.message = message;
        }
        
        @Override
        public String getEventId() {
            return eventId;
        }
        
        @Override
        public String getEventType() {
            return eventType;
        }
        
        @Override
        public LocalDateTime getOccurredOn() {
            return occurredOn;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
