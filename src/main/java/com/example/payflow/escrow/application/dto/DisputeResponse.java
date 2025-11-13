package com.example.payflow.escrow.application.dto;

import com.example.payflow.escrow.domain.Dispute;
import com.example.payflow.escrow.domain.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {
    private Long id;
    private String transactionId;
    private String reason;
    private String raisedBy;
    private DisputeStatus status;
    private String resolution;
    private String resolvedBy;
    private LocalDateTime raisedAt;
    private LocalDateTime resolvedAt;
    
    public static DisputeResponse from(Dispute dispute) {
        return new DisputeResponse(
            dispute.getId(),
            dispute.getTransactionId(),
            dispute.getReason(),
            dispute.getRaisedBy(),
            dispute.getStatus(),
            dispute.getResolution(),
            dispute.getResolvedBy(),
            dispute.getRaisedAt(),
            dispute.getResolvedAt()
        );
    }
}
