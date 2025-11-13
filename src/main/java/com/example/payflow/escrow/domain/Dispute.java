package com.example.payflow.escrow.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "escrow_disputes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dispute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = false, length = 1000)
    private String reason;
    
    @Column(nullable = false, length = 20)
    private String raisedBy;  // BUYER 또는 SELLER
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status;
    
    @Column(length = 1000)
    private String resolution;  // 해결 내용
    
    @Column(length = 100)
    private String resolvedBy;  // 관리자 ID
    
    @Column(nullable = false)
    private LocalDateTime raisedAt;
    
    private LocalDateTime resolvedAt;
    
    public Dispute(String transactionId, String reason, String raisedBy) {
        this.transactionId = transactionId;
        this.reason = reason;
        this.raisedBy = raisedBy;
        this.status = DisputeStatus.OPEN;
        this.raisedAt = LocalDateTime.now();
    }
    
    public void startReview() {
        if (this.status != DisputeStatus.OPEN) {
            throw new IllegalStateException("OPEN 상태의 분쟁만 검토를 시작할 수 있습니다.");
        }
        this.status = DisputeStatus.UNDER_REVIEW;
    }
    
    public void resolve(String resolution, String resolvedBy) {
        if (this.status == DisputeStatus.RESOLVED) {
            throw new IllegalStateException("이미 해결된 분쟁입니다.");
        }
        this.resolution = resolution;
        this.resolvedBy = resolvedBy;
        this.status = DisputeStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }
    
    public boolean isOpen() {
        return this.status == DisputeStatus.OPEN;
    }
    
    public boolean isUnderReview() {
        return this.status == DisputeStatus.UNDER_REVIEW;
    }
    
    public boolean isResolved() {
        return this.status == DisputeStatus.RESOLVED;
    }
}
