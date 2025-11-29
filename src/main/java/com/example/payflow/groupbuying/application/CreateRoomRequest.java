package com.example.payflow.groupbuying.application;

import com.example.payflow.groupbuying.domain.DeliveryFeeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 공동구매 방 생성 요청
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {
    
    private String roomTitle; // 방 제목
    private String distributorId; // 유통업자 ID
    private String distributorName; // 유통업자명
    private Long productId; // 상품 ID
    
    private BigDecimal discountRate; // 할인율 (%)
    private Integer availableStock; // 준비한 재고
    private Integer targetQuantity; // 목표 수량
    private Integer minOrderPerStore; // 가게당 최소 주문 수량
    private Integer maxOrderPerStore; // 가게당 최대 주문 수량
    
    private Integer minParticipants; // 최소 참여자 수
    private Integer maxParticipants; // 최대 참여자 수
    
    private String region; // 대상 지역
    private Long deliveryFee; // 배송비
    private DeliveryFeeType deliveryFeeType; // 배송비 타입
    private LocalDateTime expectedDeliveryDate; // 예상 배송일
    
    private LocalDateTime startTime; // 시작 시간 (null이면 즉시)
    private Integer durationHours; // 진행 시간 (시간)
    
    private String description; // 방 설명
    private String specialNote; // 특이사항
    private Boolean featured; // 추천 여부
}
