package com.example.payflow.store.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreInfoRequest {
    private String storeId;           // 매장 ID (User의 username 사용 가능)
    private String storeName;         // 매장명
    private String businessType;      // 업종 (한식, 중식, 일식, 카페, 베이커리 등)
    private String region;            // 지역 (서울 강남구, 부산 해운대구 등)
    private String mainProducts;      // 주요 품목 (쌀,채소,육류 - 콤마로 구분)
    private String description;       // 매장 설명
    private Integer employeeCount;    // 직원 수
    private String operatingHours;    // 영업시간 (예: 09:00-22:00)
    private String phoneNumber;       // 연락처
    private String address;           // 주소
}
