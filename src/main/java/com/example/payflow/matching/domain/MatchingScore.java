package com.example.payflow.matching.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingScore {
    private String distributorId;
    private String distributorName;
    private BigDecimal totalScore;        // 총점 (0-100)
    private BigDecimal regionScore;       // 지역 매칭 점수
    private BigDecimal productScore;      // 품목 매칭 점수
    private BigDecimal deliveryScore;     // 배송 가능 점수
    private BigDecimal certificationScore; // 인증 점수
    private String matchReason;           // 추천 이유
    
    // 매칭 상세 정보
    private String supplyProducts;
    private String serviceRegions;
    private Boolean deliveryAvailable;
    private String deliveryInfo;
    private String certifications;
    private Integer minOrderAmount;
    private String phoneNumber;
    private String email;
}
