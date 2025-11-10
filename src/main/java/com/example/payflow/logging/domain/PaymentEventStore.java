package com.example.payflow.logging.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이벤트 소싱 패턴 - 결제 상태 변경 이력 저장
 * 결제의 모든 상태 전이를 순차적으로 저장하여 특정 시점의 상태 재구성 가능
 */
@Entity
@Table(name = "payment_event_store", indexes = {
    @Index(name = "idx_payment_id", columnList = "paymentId"),
    @Index(name = "idx_sequence", columnList = "paymentId, sequence")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEventStore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 결제 ID
     */
    @Column(nullable = false)
    private String paymentId;
    
    /**
     * 이벤트 순서 (1부터 시작)
     */
    @Column(nullable = false)
    private Integer sequence;
    
    /**
     * 이벤트 타입
     */
    @Column(nullable = false)
    private String eventType;
    
    /**
     * 이전 상태
     */
    private String previousState;
    
    /**
     * 새로운 상태
     */
    @Column(nullable = false)
    private String newState;
    
    /**
     * 이벤트 데이터 (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String eventData;
    
    /**
     * 발생 시각
     */
    @Column(nullable = false)
    private LocalDateTime occurredAt;
    
    /**
     * 처리한 사용자/시스템
     */
    private String actor;
    
    /**
     * 메타데이터 (추가 정보)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
}
