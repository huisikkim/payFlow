package com.example.payflow.catalog.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private String distributorId;
    private String deliveryAddress;
    private String deliveryPhone;
    private String deliveryRequest; // 배송 요청사항 (선택)
}
