package com.example.payflow.logging.presentation;

import com.example.payflow.logging.application.LogAnalyticsService;
import com.example.payflow.logging.application.PaymentEventSourcingService;
import com.example.payflow.logging.application.dto.DashboardMetrics;
import com.example.payflow.logging.application.dto.RecentEventDto;
import com.example.payflow.logging.domain.EventLog;
import com.example.payflow.logging.domain.EventLogRepository;
import com.example.payflow.logging.domain.PaymentEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 로그 분석 API 컨트롤러
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogAnalyticsController {
    
    private final LogAnalyticsService analyticsService;
    private final EventLogRepository eventLogRepository;
    private final PaymentEventSourcingService eventSourcingService;
    
    /**
     * 대시보드 메트릭 조회
     */
    @GetMapping("/dashboard/metrics")
    public ResponseEntity<DashboardMetrics> getDashboardMetrics(
        @RequestParam(defaultValue = "24") int hours
    ) {
        DashboardMetrics metrics = analyticsService.getDashboardMetrics(hours);
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 실시간 이벤트 스트림
     */
    @GetMapping("/events/recent")
    public ResponseEntity<List<RecentEventDto>> getRecentEvents(
        @RequestParam(defaultValue = "50") int limit
    ) {
        List<RecentEventDto> events = analyticsService.getRecentEvents(limit);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Correlation ID로 이벤트 체인 추적
     */
    @GetMapping("/events/trace/{correlationId}")
    public ResponseEntity<List<EventLog>> traceEventChain(
        @PathVariable String correlationId
    ) {
        List<EventLog> eventChain = eventLogRepository
            .findByCorrelationIdOrderByTimestampAsc(correlationId);
        return ResponseEntity.ok(eventChain);
    }
    
    /**
     * 결제 이벤트 히스토리 조회 (이벤트 소싱)
     */
    @GetMapping("/payments/{paymentId}/history")
    public ResponseEntity<List<PaymentEventStore>> getPaymentHistory(
        @PathVariable String paymentId
    ) {
        List<PaymentEventStore> history = eventSourcingService.getPaymentHistory(paymentId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * 특정 시점의 결제 상태 재구성
     */
    @GetMapping("/payments/{paymentId}/state")
    public ResponseEntity<String> getPaymentState(
        @PathVariable String paymentId,
        @RequestParam(required = false) Integer sequence
    ) {
        String state;
        if (sequence != null) {
            state = eventSourcingService.reconstructPaymentState(paymentId, sequence);
        } else {
            state = eventSourcingService.getCurrentPaymentState(paymentId);
        }
        return ResponseEntity.ok(state);
    }
    
    /**
     * 사용자별 이벤트 조회
     */
    @GetMapping("/events/user/{userId}")
    public ResponseEntity<List<EventLog>> getUserEvents(
        @PathVariable String userId
    ) {
        List<EventLog> events = eventLogRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(events);
    }
}
