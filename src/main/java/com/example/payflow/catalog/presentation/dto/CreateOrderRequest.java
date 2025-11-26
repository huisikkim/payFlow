package com.example.payflow.catalog.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private String distributorId;
    private String deliveryAddress;
    private String deliveryPhone;
    private String deliveryRequest; // 배송 요청사항 (선택)
    private LocalDateTime desiredDeliveryDate; // 희망 배송일 (선택)
}
