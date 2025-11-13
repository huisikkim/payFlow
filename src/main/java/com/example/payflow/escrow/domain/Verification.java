package com.example.payflow.escrow.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "escrow_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Verification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationResult result;
    
    @Column(length = 100)
    private String verifiedBy;  // 검증자 ID
    
    @Column(length = 1000)
    private String notes;  // 검증 메모
    
    @Column(length = 100)
    private String documentId;  // 증빙 문서 ID
    
    @Column(nullable = false)
    private LocalDateTime verifiedAt;
    
    public Verification(String transactionId, VerificationType type, VerificationResult result, String verifiedBy) {
        this.transactionId = transactionId;
        this.type = type;
        this.result = result;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = LocalDateTime.now();
    }
    
    public Verification(String transactionId, VerificationType type, VerificationResult result, 
                       String verifiedBy, String notes, String documentId) {
        this(transactionId, type, result, verifiedBy);
        this.notes = notes;
        this.documentId = documentId;
    }
    
    public boolean isPassed() {
        return this.result == VerificationResult.PASSED;
    }
    
    public boolean isFailed() {
        return this.result == VerificationResult.FAILED;
    }
}
