package com.example.payflow.logging.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 중앙 집중식 이벤트 로그
 * 모든 비즈니스 이벤트를 추적하고 저장
 */
@Entity
@Table(name = "event_logs", indexes = {
    @Index(name = "idx_correlation_id", columnList = "correlationId"),
    @Index(name = "idx_event_type", columnList = "eventType"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Correlation ID - 하나의 요청이 여러 서비스를 거치는 과정을 추적
     */
    @Column(nullable = false)
    private String correlationId;
    
    /**
     * 이벤트 타입 (OrderCreated, PaymentApproved, SettlementCompleted 등)
     */
    @Column(nullable = false)
    private String eventType;
    
    /**
     * 이벤트가 발생한 서비스 (order, payment, stage 등)
     */
    @Column(nullable = false)
    private String serviceName;
    
    /**
     * 이벤트 페이로드 (JSON 형식)
     */
    @Column(columnDefinition = "TEXT")
    private String payload;
    
    /**
     * 이벤트 발생 시각
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * 사용자 ID (추적용)
     */
    private String userId;
    
    /**
     * 이벤트 상태 (SUCCESS, FAILED, PROCESSING)
     */
    @Enumerated(EnumType.STRING)
    private EventStatus status;
    
    /**
     * 에러 메시지 (실패 시)
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 처리 시간 (밀리초)
     */
    private Long processingTimeMs;
}
