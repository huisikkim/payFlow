package com.example.payflow.sessionreplay.presentation;

import com.example.payflow.sessionreplay.application.EventCollectorService;
import com.example.payflow.sessionreplay.application.dto.InteractionEventDto;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 이벤트 수집 REST API Controller
 */
@RestController
@RequestMapping("/api/session-replay")
@RequiredArgsConstructor
@Slf4j
public class EventCollectorController {
    
    private final EventCollectorService eventCollectorService;
    private final RateLimiter rateLimiter;
    
    /**
     * 이벤트 배치 수신
     */
    @PostMapping("/events")
    public ResponseEntity<Void> collectEvents(@RequestBody @Valid List<InteractionEventDto> events) {
        // Rate limiting 체크
        if (!rateLimiter.tryAcquire()) {
            log.warn("Rate limit exceeded for event collection");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
        log.debug("Received {} events", events.size());
        
        // 이벤트를 Kafka로 발행
        eventCollectorService.publishEvents(events);
        
        return ResponseEntity.accepted().build();
    }
}
