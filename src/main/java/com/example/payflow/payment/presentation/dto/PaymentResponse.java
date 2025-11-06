package com.example.payflow.payment.presentation.dto;

import com.example.payflow.payment.domain.Payment;
import com.example.payflow.payment.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private Long amount;
    private PaymentStatus status;
    private String method;
    private LocalDateTime approvedAt;
    
    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
            .paymentKey(payment.getPaymentKey())
            .orderId(payment.getOrderId())
            .orderName(payment.getOrderName())
            .amount(payment.getAmount())
            .status(payment.getStatus())
            .method(payment.getMethod())
            .approvedAt(payment.getApprovedAt())
            .build();
    }
}
