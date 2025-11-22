package com.example.payflow.payment.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private Long totalAmount;
    private String method;
    private String status;
    private LocalDateTime approvedAt;
    private VirtualAccountInfo virtualAccount;
    private java.util.List<CancelInfo> cancels;
    
    @Getter
    @NoArgsConstructor
    public static class VirtualAccountInfo {
        private String accountNumber;
        private String bankCode;
        private String bank;
        private String customerName;
        private String dueDate;
        private String refundStatus;
    }
    
    @Getter
    @NoArgsConstructor
    public static class CancelInfo {
        private String cancelReason;
        private Long cancelAmount;
        private String canceledAt;
    }
}
