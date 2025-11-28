package com.example.payflow.catalog.presentation.dto;

import com.example.payflow.catalog.domain.DeliveryStatus;
import com.example.payflow.catalog.domain.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponse {
    
    private Long id;
    
    private Long orderId;
    
    private String orderNumber;
    
    private String storeId;
    
    private String distributorId;
    
    private DeliveryType deliveryType;
    
    private String deliveryTypeDescription;
    
    // 택배 배송 정보
    private String trackingNumber;
    
    private String courierCompany;
    
    private String courierPhone;
    
    // 직접 배송 정보
    private String driverName;
    
    private String driverPhone;
    
    private String vehicleNumber;
    
    private DeliveryStatus status;
    
    private String statusDescription;
    
    private LocalDateTime preparedAt;
    
    private LocalDateTime shippedAt;
    
    private LocalDateTime deliveredAt;
    
    private LocalDateTime estimatedDeliveryDate;
    
    private String deliveryAddress;
    
    private String deliveryPhone;
    
    private String deliveryRequest;
    
    private String deliveryNotes;
    
    private Long totalAmount;
    
    private LocalDateTime createdAt;
}
