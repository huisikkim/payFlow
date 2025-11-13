package com.example.payflow.escrow.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "escrow_settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal feeAmount;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal sellerAmount;
    
    @Column(nullable = false, length = 100)
    private String sellerId;
    
    @Column(length = 50)
    private String paymentMethod;  // 정산 방법
    
    @Column(length = 100)
    private String paymentReference;  // 정산 참조번호
    
    @Column(nullable = false)
    private LocalDateTime initiatedAt;
    
    private LocalDateTime completedAt;
    
    @Column(length = 500)
    private String failureReason;
    
    public Settlement(String transactionId, BigDecimal totalAmount, BigDecimal feeAmount, 
                     BigDecimal sellerAmount, String sellerId) {
        this.transactionId = transactionId;
        this.totalAmount = totalAmount;
        this.feeAmount = feeAmount;
        this.sellerAmount = sellerAmount;
        this.sellerId = sellerId;
        this.initiatedAt = LocalDateTime.now();
    }
    
    public void complete(String paymentMethod, String paymentReference) {
        if (this.completedAt != null) {
            throw new IllegalStateException("이미 완료된 정산입니다.");
        }
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.completedAt = LocalDateTime.now();
    }
    
    public void markFailed(String reason) {
        if (this.completedAt != null) {
            throw new IllegalStateException("이미 완료된 정산은 실패 처리할 수 없습니다.");
        }
        this.failureReason = reason;
    }
    
    public boolean isCompleted() {
        return this.completedAt != null;
    }
    
    public boolean isFailed() {
        return this.failureReason != null && this.completedAt == null;
    }
}
