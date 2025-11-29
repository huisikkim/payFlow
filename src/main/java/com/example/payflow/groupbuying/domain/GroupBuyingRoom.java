package com.example.payflow.groupbuying.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 공동구매 방
 * 유통업자가 생성하고 가게들이 참여하는 공동구매 방
 */
@Entity
@Table(name = "group_buying_rooms", indexes = {
    @Index(name = "idx_distributor_status", columnList = "distributorId,status"),
    @Index(name = "idx_region_status", columnList = "region,status"),
    @Index(name = "idx_deadline", columnList = "deadline"),
    @Index(name = "idx_product", columnList = "productId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBuyingRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String roomId; // 방 고유 ID (예: GBR-20231129-001)
    
    @Column(nullable = false)
    private String roomTitle; // 방 제목 (예: "김치 대박 세일! 20% 할인")
    
    // === 유통업자 정보 ===
    @Column(nullable = false)
    private String distributorId; // 유통업자 ID
    
    @Column(nullable = false)
    private String distributorName; // 유통업자명
    
    // === 상품 정보 ===
    @Column(nullable = false)
    private Long productId; // 상품 ID (ProductCatalog)
    
    @Column(nullable = false)
    private String productName; // 상품명
    
    @Column(nullable = false)
    private String category; // 카테고리
    
    @Column(nullable = false)
    private String unit; // 단위 (kg, 개, 박스 등)
    
    @Column
    private String origin; // 원산지
    
    @Column(columnDefinition = "TEXT")
    private String productDescription; // 상품 설명
    
    @Column
    private String imageUrl; // 상품 이미지
    
    // === 가격 정보 ===
    @Column(nullable = false)
    private Long originalPrice; // 원가 (단가)
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountRate; // 할인율 (%)
    
    @Column(nullable = false)
    private Long discountedPrice; // 할인된 가격
    
    // === 재고 및 목표 ===
    @Column(nullable = false)
    private Integer availableStock; // 유통업자가 준비한 재고
    
    @Column(nullable = false)
    private Integer targetQuantity; // 목표 수량 (최소 판매 수량)
    
    @Column(nullable = false)
    @Builder.Default
    private Integer currentQuantity = 0; // 현재 신청된 수량
    
    @Column(nullable = false)
    private Integer minOrderPerStore; // 가게당 최소 주문 수량
    
    @Column
    private Integer maxOrderPerStore; // 가게당 최대 주문 수량
    
    // === 참여자 정보 ===
    @Column(nullable = false)
    private Integer minParticipants; // 최소 참여자 수
    
    @Column
    private Integer maxParticipants; // 최대 참여자 수
    
    @Column(nullable = false)
    @Builder.Default
    private Integer currentParticipants = 0; // 현재 참여자 수
    
    // === 지역 및 배송 ===
    @Column(nullable = false)
    private String region; // 대상 지역 (예: "서울 강남구,서초구" - 콤마로 구분)
    
    @Column
    private Long deliveryFee; // 배송비 (총액)
    
    @Column
    private Long deliveryFeePerStore; // 가게당 배송비
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryFeeType deliveryFeeType = DeliveryFeeType.SHARED; // 배송비 타입
    
    @Column
    private LocalDateTime expectedDeliveryDate; // 예상 배송일
    
    // === 시간 정보 ===
    @Column(nullable = false)
    private LocalDateTime startTime; // 시작 시간
    
    @Column(nullable = false)
    private LocalDateTime deadline; // 마감 시간
    
    @Column(nullable = false)
    private Integer durationHours; // 진행 시간 (시간)
    
    // === 상태 ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.WAITING; // 방 상태
    
    @Column
    private LocalDateTime openedAt; // 오픈 시간
    
    @Column
    private LocalDateTime closedAt; // 마감 시간
    
    @Column
    private LocalDateTime cancelledAt; // 취소 시간
    
    @Column
    private String cancellationReason; // 취소 사유
    
    // === 추가 정보 ===
    @Column(columnDefinition = "TEXT")
    private String description; // 방 설명 (유통업자가 작성)
    
    @Column(columnDefinition = "TEXT")
    private String specialNote; // 특이사항 (예: "신선도 보장", "당일 배송")
    
    @Column
    private Boolean featured; // 추천 여부 (메인에 노출)
    
    @Column
    private Integer viewCount; // 조회수
    
    // === 참여자 목록 ===
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupBuyingParticipant> participants = new ArrayList<>();
    
    // === 생성 정보 ===
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (viewCount == null) {
            viewCount = 0;
        }
        if (featured == null) {
            featured = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 방 오픈
     */
    public void open() {
        if (status != RoomStatus.WAITING) {
            throw new IllegalStateException("대기 중인 방만 오픈할 수 있습니다.");
        }
        this.status = RoomStatus.OPEN;
        this.openedAt = LocalDateTime.now();
    }
    
    /**
     * 참여자 추가
     */
    public void addParticipant(GroupBuyingParticipant participant) {
        if (!canJoin()) {
            throw new IllegalStateException("참여할 수 없는 방입니다.");
        }
        
        if (maxParticipants != null && currentParticipants >= maxParticipants) {
            throw new IllegalStateException("최대 참여자 수를 초과했습니다.");
        }
        
        if (currentQuantity + participant.getQuantity() > availableStock) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        
        participants.add(participant);
        participant.setRoom(this);
        this.currentParticipants++;
        this.currentQuantity += participant.getQuantity();
        
        calculateDeliveryFeePerStore();
        checkAutoClose();
    }
    
    /**
     * 참여자 제거
     */
    public void removeParticipant(GroupBuyingParticipant participant) {
        participants.remove(participant);
        this.currentParticipants--;
        this.currentQuantity -= participant.getQuantity();
        calculateDeliveryFeePerStore();
    }
    
    /**
     * 가게당 배송비 계산
     */
    private void calculateDeliveryFeePerStore() {
        if (deliveryFeeType == DeliveryFeeType.SHARED && deliveryFee != null && currentParticipants > 0) {
            this.deliveryFeePerStore = deliveryFee / currentParticipants;
        } else if (deliveryFeeType == DeliveryFeeType.FREE) {
            this.deliveryFeePerStore = 0L;
        } else if (deliveryFeeType == DeliveryFeeType.FIXED) {
            this.deliveryFeePerStore = deliveryFee;
        }
    }
    
    /**
     * 목표 달성 여부
     */
    public boolean isTargetReached() {
        return currentQuantity >= targetQuantity && currentParticipants >= minParticipants;
    }
    
    /**
     * 재고 소진 여부
     */
    public boolean isStockFull() {
        return currentQuantity >= availableStock;
    }
    
    /**
     * 자동 마감 체크 (재고 소진 시)
     */
    private void checkAutoClose() {
        if (isStockFull() && status == RoomStatus.OPEN) {
            close();
        }
    }
    
    /**
     * 방 마감
     */
    public void close() {
        if (status != RoomStatus.OPEN) {
            throw new IllegalStateException("오픈된 방만 마감할 수 있습니다.");
        }
        
        if (isTargetReached()) {
            this.status = RoomStatus.CLOSED_SUCCESS;
        } else {
            this.status = RoomStatus.CLOSED_FAILED;
        }
        this.closedAt = LocalDateTime.now();
    }
    
    /**
     * 방 취소
     */
    public void cancel(String reason) {
        this.status = RoomStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
    
    /**
     * 주문 생성 완료
     */
    public void completeOrders() {
        if (status != RoomStatus.CLOSED_SUCCESS) {
            throw new IllegalStateException("성공적으로 마감된 방만 주문을 생성할 수 있습니다.");
        }
        this.status = RoomStatus.ORDER_CREATED;
    }
    
    /**
     * 참여 가능 여부
     */
    public boolean canJoin() {
        return status == RoomStatus.OPEN && 
               !isExpired() && 
               !isStockFull() &&
               (maxParticipants == null || currentParticipants < maxParticipants);
    }
    
    /**
     * 마감 시간 지났는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }
    
    /**
     * 달성률 계산 (%)
     */
    public BigDecimal getAchievementRate() {
        if (targetQuantity == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(currentQuantity)
                .divide(BigDecimal.valueOf(targetQuantity), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 재고 잔여율 계산 (%)
     */
    public BigDecimal getStockRemainRate() {
        if (availableStock == 0) return BigDecimal.ZERO;
        int remaining = availableStock - currentQuantity;
        return BigDecimal.valueOf(remaining)
                .divide(BigDecimal.valueOf(availableStock), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
    }
    
    /**
     * 추천 설정
     */
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}
