package com.example.payflow.escrow.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "escrow_event_store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EscrowEventStore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = false)
    private Integer sequence;
    
    @Column(nullable = false, length = 50)
    private String eventType;
    
    @Column(length = 30)
    private String previousStatus;
    
    @Column(length = 30)
    private String newStatus;
    
    @Column(columnDefinition = "TEXT")
    private String eventData;  // JSON 형식
    
    @Column(nullable = false, length = 100)
    private String triggeredBy;
    
    @Column(nullable = false)
    private LocalDateTime occurredAt;
    
    public EscrowEventStore(String transactionId, Integer sequence, String eventType,
                           String previousStatus, String newStatus, String eventData,
                           String triggeredBy) {
        this.transactionId = transactionId;
        this.sequence = sequence;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.eventData = eventData;
        this.triggeredBy = triggeredBy;
        this.occurredAt = LocalDateTime.now();
    }
}
