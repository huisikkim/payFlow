package com.example.payflow.escrow.application.dto;

import com.example.payflow.escrow.domain.VerificationResult;
import com.example.payflow.escrow.domain.VerificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {
    private String transactionId;
    private VerificationType type;
    private VerificationResult result;
    private String verifiedBy;
    private String notes;
    private String documentId;
}
