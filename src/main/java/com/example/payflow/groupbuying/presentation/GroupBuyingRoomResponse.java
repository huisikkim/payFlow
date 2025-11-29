package com.example.payflow.groupbuying.presentation;

import com.example.payflow.groupbuying.domain.DeliveryFeeType;
import com.example.payflow.groupbuying.domain.GroupBuyingRoom;
import com.example.payflow.groupbuying.domain.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 공동구매 방 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBuyingRoomResponse {
    
    private Long id;
    private String roomId;
    private String roomTitle;
    
    // 유통업자 정보
    private String distributorId;
    private String distributorName;
    
    // 상품 정보
    private Long productId;
    private String productName;
    private String category;
    private String unit;
    private String origin;
    private String productDescription;
    private String imageUrl;
    
    // 가격 정보
    private Long originalPrice;
    private BigDecimal discountRate;
    private Long discountedPrice;
    private Long savingsPerUnit; // 단위당 절감액
    
    // 재고 및 목표
    private Integer availableStock;
    private Integer targetQuantity;
    private Integer currentQuantity;
    private Integer minOrderPerStore;
    private Integer maxOrderPerStore;
    
    // 참여자 정보
    private Integer minParticipants;
    private Integer maxParticipants;
    private Integer currentParticipants;
    
    // 진행률
    private BigDecimal achievementRate; // 목표 달성률 (%)
    private BigDecimal stockRemainRate; // 재고 잔여율 (%)
    
    // 지역 및 배송
    private String region;
    private Long deliveryFee;
    private Long deliveryFeePerStore;
    private DeliveryFeeType deliveryFeeType;
    private LocalDateTime expectedDeliveryDate;
    
    // 시간 정보
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private Integer durationHours;
    private Long remainingMinutes; // 남은 시간 (분)
    
    // 상태
    private RoomStatus status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    
    // 추가 정보
    private String description;
    private String specialNote;
    private Boolean featured;
    private Integer viewCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static GroupBuyingRoomResponse from(GroupBuyingRoom room) {
        // 남은 시간 계산
        Long remainingMinutes = null;
        if (room.getStatus() == RoomStatus.OPEN) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(room.getDeadline())) {
                remainingMinutes = java.time.Duration.between(now, room.getDeadline()).toMinutes();
            }
        }
        
        // 단위당 절감액
        Long savingsPerUnit = room.getOriginalPrice() - room.getDiscountedPrice();
        
        return GroupBuyingRoomResponse.builder()
                .id(room.getId())
                .roomId(room.getRoomId())
                .roomTitle(room.getRoomTitle())
                .distributorId(room.getDistributorId())
                .distributorName(room.getDistributorName())
                .productId(room.getProductId())
                .productName(room.getProductName())
                .category(room.getCategory())
                .unit(room.getUnit())
                .origin(room.getOrigin())
                .productDescription(room.getProductDescription())
                .imageUrl(room.getImageUrl())
                .originalPrice(room.getOriginalPrice())
                .discountRate(room.getDiscountRate())
                .discountedPrice(room.getDiscountedPrice())
                .savingsPerUnit(savingsPerUnit)
                .availableStock(room.getAvailableStock())
                .targetQuantity(room.getTargetQuantity())
                .currentQuantity(room.getCurrentQuantity())
                .minOrderPerStore(room.getMinOrderPerStore())
                .maxOrderPerStore(room.getMaxOrderPerStore())
                .minParticipants(room.getMinParticipants())
                .maxParticipants(room.getMaxParticipants())
                .currentParticipants(room.getCurrentParticipants())
                .achievementRate(room.getAchievementRate())
                .stockRemainRate(room.getStockRemainRate())
                .region(room.getRegion())
                .deliveryFee(room.getDeliveryFee())
                .deliveryFeePerStore(room.getDeliveryFeePerStore())
                .deliveryFeeType(room.getDeliveryFeeType())
                .expectedDeliveryDate(room.getExpectedDeliveryDate())
                .startTime(room.getStartTime())
                .deadline(room.getDeadline())
                .durationHours(room.getDurationHours())
                .remainingMinutes(remainingMinutes)
                .status(room.getStatus())
                .openedAt(room.getOpenedAt())
                .closedAt(room.getClosedAt())
                .description(room.getDescription())
                .specialNote(room.getSpecialNote())
                .featured(room.getFeatured())
                .viewCount(room.getViewCount())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
