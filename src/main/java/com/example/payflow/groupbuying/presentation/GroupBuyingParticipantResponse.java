package com.example.payflow.groupbuying.presentation;

import com.example.payflow.groupbuying.domain.GroupBuyingParticipant;
import com.example.payflow.groupbuying.domain.ParticipantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공동구매 참여자 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBuyingParticipantResponse {
    
    private Long id;
    private String storeId;
    private String storeName;
    private String storeRegion;
    
    private Integer quantity;
    private Long unitPrice;
    private Long totalProductAmount;
    private Long deliveryFee;
    private Long totalAmount;
    private Long savingsAmount;
    
    private String deliveryAddress;
    private String deliveryPhone;
    private String deliveryRequest;
    
    private ParticipantStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime confirmedAt;
    
    private Long distributorOrderId;
    private LocalDateTime orderCreatedAt;
    
    public static GroupBuyingParticipantResponse from(GroupBuyingParticipant participant) {
        return GroupBuyingParticipantResponse.builder()
                .id(participant.getId())
                .storeId(participant.getStoreId())
                .storeName(participant.getStoreName())
                .storeRegion(participant.getStoreRegion())
                .quantity(participant.getQuantity())
                .unitPrice(participant.getUnitPrice())
                .totalProductAmount(participant.getTotalProductAmount())
                .deliveryFee(participant.getDeliveryFee())
                .totalAmount(participant.getTotalAmount())
                .savingsAmount(participant.getSavingsAmount())
                .deliveryAddress(participant.getDeliveryAddress())
                .deliveryPhone(participant.getDeliveryPhone())
                .deliveryRequest(participant.getDeliveryRequest())
                .status(participant.getStatus())
                .joinedAt(participant.getJoinedAt())
                .confirmedAt(participant.getConfirmedAt())
                .distributorOrderId(participant.getDistributorOrderId())
                .orderCreatedAt(participant.getOrderCreatedAt())
                .build();
    }
}
