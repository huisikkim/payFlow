package com.example.payflow.escrow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryConfirmRequest {
    private String transactionId;
    private String confirmedBy;  // 판매자 ID
}
