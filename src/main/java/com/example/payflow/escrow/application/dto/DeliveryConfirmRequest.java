package com.example.payflow.escrow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryConfirmRequest {
    private String transactionId;
    private String confirmedBy;  // 판매자 ID
    private LocalDateTime deliveredAt;  // 인도 시간
    private String deliveryLocation;  // 인도 장소
    private String deliveryNotes;  // 인도 메모
}
