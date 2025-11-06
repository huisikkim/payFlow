package com.example.payflow.payment.infrastructure.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private Long totalAmount;
    private String method;
    private String status;
    private String approvedAt;
}
