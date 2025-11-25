package com.example.payflow.store.presentation.dto;

import com.example.payflow.store.domain.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInfoResponse {
    private Long id;
    private String storeId;
    private String storeName;
    private String ownerName;
    private String phoneNumber;
    private String address;
    private String businessNumber;
    private String businessType;
    private String region;
    private String mainProducts;
    private String description;
    private Integer employeeCount;
    private String operatingHours;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static StoreInfoResponse from(Store store) {
        return StoreInfoResponse.builder()
                .id(store.getId())
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .ownerName(store.getOwnerName())
                .phoneNumber(store.getPhoneNumber())
                .address(store.getAddress())
                .businessNumber(store.getBusinessNumber())
                .businessType(store.getBusinessType())
                .region(store.getRegion())
                .mainProducts(store.getMainProducts())
                .description(store.getDescription())
                .employeeCount(store.getEmployeeCount())
                .operatingHours(store.getOperatingHours())
                .isActive(store.getIsActive())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
