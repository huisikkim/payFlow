package com.example.payflow.sessionreplay.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상호작용 이벤트 Entity
 * 사용자의 개별 상호작용을 나타냄
 */
@Entity
@Table(name = "interaction_events", indexes = {
    @Index(name = "idx_ie_session_id", columnList = "sessionId"),
    @Index(name = "idx_ie_timestamp", columnList = "timestamp"),
    @Index(name = "idx_ie_event_type", columnList = "eventType")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractionEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sessionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;
    
    @Column(nullable = false)
    private Long timestamp; // Unix timestamp in milliseconds
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload; // JSON payload (compressed)
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
