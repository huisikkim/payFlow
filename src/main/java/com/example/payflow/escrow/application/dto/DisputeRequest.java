package com.example.payflow.escrow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DisputeRequest {
    private String transactionId;
    private String reason;
    private String raisedBy;  // BUYER 또는 SELLER
}
