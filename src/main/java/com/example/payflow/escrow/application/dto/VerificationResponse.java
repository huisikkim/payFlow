package com.example.payflow.escrow.application.dto;

import com.example.payflow.escrow.domain.Verification;
import com.example.payflow.escrow.domain.VerificationResult;
import com.example.payflow.escrow.domain.VerificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {
    private Long id;
    private String transactionId;
    private VerificationType type;
    private VerificationResult result;
    private String verifiedBy;
    private String notes;
    private String documentId;
    private LocalDateTime verifiedAt;
    
    public static VerificationResponse from(Verification verification) {
        return new VerificationResponse(
            verification.getId(),
            verification.getTransactionId(),
            verification.getType(),
            verification.getResult(),
            verification.getVerifiedBy(),
            verification.getNotes(),
            verification.getDocumentId(),
            verification.getVerifiedAt()
        );
    }
}
