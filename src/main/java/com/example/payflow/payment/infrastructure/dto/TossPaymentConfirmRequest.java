package com.example.payflow.payment.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TossPaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private Long amount;
}
