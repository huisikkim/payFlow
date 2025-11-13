package com.example.payflow.escrow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OwnershipTransferRequest {
    private String transactionId;
    private String verifiedBy;
    private String documentId;
    private String notes;
}
