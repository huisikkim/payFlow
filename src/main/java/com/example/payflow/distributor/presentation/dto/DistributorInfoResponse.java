package com.example.payflow.distributor.presentation.dto;

import com.example.payflow.distributor.domain.Distributor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributorInfoResponse {
    private Long id;
    private String distributorId;
    private String distributorName;
    private String businessNumber;
    private String phoneNumber;
    private String managerName;
    private String email;
    private String supplyProducts;
    private String serviceRegions;
    private Boolean deliveryAvailable;
    private String deliveryInfo;
    private String description;
    private String certifications;
    private Integer minOrderAmount;
    private String operatingHours;
    private String address;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static DistributorInfoResponse from(Distributor distributor) {
        return DistributorInfoResponse.builder()
                .id(distributor.getId())
                .distributorId(distributor.getDistributorId())
                .distributorName(distributor.getDistributorName())
                .businessNumber(distributor.getBusinessNumber())
                .phoneNumber(distributor.getPhoneNumber())
                .managerName(distributor.getManagerName())
                .email(distributor.getEmail())
                .supplyProducts(distributor.getSupplyProducts())
                .serviceRegions(distributor.getServiceRegions())
                .deliveryAvailable(distributor.getDeliveryAvailable())
                .deliveryInfo(distributor.getDeliveryInfo())
                .description(distributor.getDescription())
                .certifications(distributor.getCertifications())
                .minOrderAmount(distributor.getMinOrderAmount())
                .operatingHours(distributor.getOperatingHours())
                .address(distributor.getAddress())
                .isActive(distributor.getIsActive())
                .createdAt(distributor.getCreatedAt())
                .updatedAt(distributor.getUpdatedAt())
                .build();
    }
}
