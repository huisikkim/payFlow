package com.example.payflow.catalog.presentation.dto;

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
public class StartShippingRequest {
    
    private DeliveryType deliveryType; // 배송 방식 (DIRECT: 직접배송, COURIER: 택배배송)
    
    // 택배 배송 시 필수
    private String trackingNumber; // 송장번호
    private String courierCompany; // 배송사 (CJ대한통운, 로젠택배, 한진택배, 우체국택배)
    private String courierPhone; // 배송사 연락처
    
    // 직접 배송 시 필수
    private String driverName; // 배송 기사 이름
    private String driverPhone; // 배송 기사 연락처
    private String vehicleNumber; // 차량 번호
    
    // 공통
    private LocalDateTime estimatedDeliveryDate; // 예상 배송일
    private String deliveryNotes; // 배송 메모
}
