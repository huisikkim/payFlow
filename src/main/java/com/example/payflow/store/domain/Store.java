package com.example.payflow.store.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String storeId;
    
    @Column(nullable = false)
    private String storeName;
    
    @Column(nullable = false)
    private String ownerName;
    
    private String phoneNumber;
    private String address;
    private String businessNumber;
    
    // 매장 상세 정보
    private String businessType;      // 업종 (한식, 중식, 일식, 카페 등)
    private String region;            // 지역 (서울 강남구, 부산 해운대구 등)
    private String mainProducts;      // 주요 품목 (쌀, 채소, 육류 등 - 콤마로 구분)
    private String description;       // 매장 설명
    private Integer employeeCount;    // 직원 수
    private String operatingHours;    // 영업시간
    
    private Boolean isActive = true;  // 활성화 여부 (기존 데이터 호환을 위해 nullable)
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Store(String storeId, String storeName, String ownerName, String phoneNumber, String address) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.ownerName = ownerName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.isActive = true;  // 기본값 설정
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 매장 정보 업데이트
    public void updateStoreInfo(String businessType, String region, String mainProducts, 
                               String description, Integer employeeCount, String operatingHours) {
        this.businessType = businessType;
        this.region = region;
        this.mainProducts = mainProducts;
        this.description = description;
        this.employeeCount = employeeCount;
        this.operatingHours = operatingHours;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 기본 정보 업데이트
    public void updateBasicInfo(String storeName, String phoneNumber, String address) {
        this.storeName = storeName;
        this.phoneNumber = phoneNumber;
        this.address = address;
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
