package com.example.payflow.escrow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositRequest {
    private String transactionId;
    private BigDecimal amount;
    private String depositMethod;
    private String depositReference;
}
