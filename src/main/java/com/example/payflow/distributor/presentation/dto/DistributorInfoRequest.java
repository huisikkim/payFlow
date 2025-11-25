package com.example.payflow.distributor.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributorInfoRequest {
    private String distributorId;      // 유통업체 ID (User의 username 사용 가능)
    private String distributorName;    // 업체명
    private String supplyProducts;     // 공급 품목 (쌀/곡물,채소,육류 - 콤마로 구분)
    private String serviceRegions;     // 서비스 지역 (서울,경기,인천 - 콤마로 구분)
    private Boolean deliveryAvailable; // 배송 가능 여부
    private String deliveryInfo;       // 배송 정보 (배송비, 최소 주문 금액 등)
    private String description;        // 업체 소개
    private String certifications;     // 인증 정보 (HACCP,ISO - 콤마로 구분)
    private Integer minOrderAmount;    // 최소 주문 금액
    private String operatingHours;     // 운영 시간 (예: 09:00-18:00)
    private String phoneNumber;        // 연락처
    private String email;              // 이메일
    private String address;            // 주소
}
