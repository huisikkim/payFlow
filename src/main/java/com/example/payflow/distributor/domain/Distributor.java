package com.example.payflow.distributor.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "distributors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Distributor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String distributorId;
    
    @Column(nullable = false)
    private String distributorName;
    
    private String businessNumber;
    private String phoneNumber;
    private String managerName;
    private String email;
    
    // 유통업체 상세 정보
    private String supplyProducts;     // 공급 품목 (쌀/곡물,채소,육류 - 콤마로 구분)
    private String serviceRegions;     // 서비스 지역 (서울,경기,인천 - 콤마로 구분)
    private Boolean deliveryAvailable = true;  // 배송 가능 여부
    private String deliveryInfo;       // 배송 정보 (배송비, 최소 주문 금액 등)
    private String description;        // 업체 소개
    private String certifications;     // 인증 정보 (HACCP, ISO 등 - 콤마로 구분)
    private Integer minOrderAmount;    // 최소 주문 금액
    private String operatingHours;     // 운영 시간
    private String address;            // 주소
    
    private Boolean isActive = true;   // 활성화 여부
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Distributor(String distributorId, String distributorName, String businessNumber, 
                      String phoneNumber, String managerName) {
        this.distributorId = distributorId;
        this.distributorName = distributorName;
        this.businessNumber = businessNumber;
        this.phoneNumber = phoneNumber;
        this.managerName = managerName;
        this.isActive = true;
        this.deliveryAvailable = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 유통업체 정보 업데이트
    public void updateDistributorInfo(String supplyProducts, String serviceRegions, 
                                     Boolean deliveryAvailable, String deliveryInfo,
                                     String description, String certifications,
                                     Integer minOrderAmount, String operatingHours, String address) {
        this.supplyProducts = supplyProducts;
        this.serviceRegions = serviceRegions;
        this.deliveryAvailable = deliveryAvailable;
        this.deliveryInfo = deliveryInfo;
        this.description = description;
        this.certifications = certifications;
        this.minOrderAmount = minOrderAmount;
        this.operatingHours = operatingHours;
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 기본 정보 업데이트
    public void updateBasicInfo(String distributorName, String phoneNumber, String email) {
        this.distributorName = distributorName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
