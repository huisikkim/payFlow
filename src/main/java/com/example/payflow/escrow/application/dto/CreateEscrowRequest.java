package com.example.payflow.escrow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEscrowRequest {
    private ParticipantDto buyer;
    private ParticipantDto seller;
    private VehicleDto vehicle;
    private BigDecimal amount;
    private BigDecimal feeRate;  // Optional, 기본값 0.03 (3%)
}
