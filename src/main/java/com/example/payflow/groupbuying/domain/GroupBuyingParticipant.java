package com.example.payflow.groupbuying.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 공동구매 참여자
 * 가게가 공동구매 방에 참여한 정보
 */
@Entity
@Table(name = "group_buying_participants", indexes = {
    @Index(name = "idx_room_store", columnList = "roomId,storeId"),
    @Index(name = "idx_store_status", columnList = "storeId,status")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"roomId", "storeId"}) // 한 방에 한 가게는 한 번만 참여
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBuyingParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private GroupBuyingRoom room;
    
    @Column(nullable = false)
    private String storeId; // 가게 ID
    
    @Column(nullable = false)
    private String storeName; // 가게명
    
    @Column
    private String storeRegion; // 가게 지역
    
    @Column
    private String storePhone; // 가게 연락처
    
    // === 주문 정보 ===
    @Column(nullable = false)
    private Integer quantity; // 주문 수량
    
    @Column(nullable = false)
    private Long unitPrice; // 단가 (할인 적용된 가격)
    
    @Column(nullable = false)
    private Long totalProductAmount; // 상품 총액
    
    @Column
    private Long deliveryFee; // 배송비 분담액
    
    @Column(nullable = false)
    private Long totalAmount; // 최종 총액 (상품 + 배송비)
    
    @Column(nullable = false)
    private Long savingsAmount; // 절감액 (원가 대비)
    
    // === 배송 정보 ===
    @Column(nullable = false)
    private String deliveryAddress; // 배송 주소
    
    @Column(nullable = false)
    private String deliveryPhone; // 배송 연락처
    
    @Column(columnDefinition = "TEXT")
    private String deliveryRequest; // 배송 요청사항
    
    // === 상태 ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.JOINED; // 참여 상태
    
    @Column(nullable = false)
    private LocalDateTime joinedAt; // 참여 시간
    
    @Column
    private LocalDateTime confirmedAt; // 확정 시간 (방 마감 시)
    
    @Column
    private LocalDateTime cancelledAt; // 취소 시간
    
    @Column
    private String cancellationReason; // 취소 사유
    
    // === 주문 생성 정보 ===
    @Column
    private Long distributorOrderId; // 생성된 주문 ID (확정 후)
    
    @Column
    private LocalDateTime orderCreatedAt; // 주문 생성 시간
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
    
    /**
     * 참여 확정 (방 마감 시 자동 호출)
     */
    public void confirm() {
        if (status != ParticipantStatus.JOINED) {
            throw new IllegalStateException("참여 중인 상태만 확정할 수 있습니다.");
        }
        this.status = ParticipantStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }
    
    /**
     * 참여 취소
     */
    public void cancel(String reason) {
        if (status == ParticipantStatus.ORDER_CREATED) {
            throw new IllegalStateException("이미 주문이 생성되어 취소할 수 없습니다.");
        }
        this.status = ParticipantStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
    
    /**
     * 주문 생성 완료
     */
    public void completeOrder(Long orderId) {
        if (status != ParticipantStatus.CONFIRMED) {
            throw new IllegalStateException("확정된 참여만 주문을 생성할 수 있습니다.");
        }
        this.distributorOrderId = orderId;
        this.status = ParticipantStatus.ORDER_CREATED;
        this.orderCreatedAt = LocalDateTime.now();
    }
    
    /**
     * 배송 완료
     */
    public void completeDelivery() {
        if (status != ParticipantStatus.ORDER_CREATED) {
            throw new IllegalStateException("주문이 생성된 참여만 배송 완료할 수 있습니다.");
        }
        this.status = ParticipantStatus.DELIVERED;
    }
    
    /**
     * 총액 계산
     */
    public void calculateTotalAmount() {
        this.totalProductAmount = unitPrice * quantity;
        this.totalAmount = totalProductAmount + (deliveryFee != null ? deliveryFee : 0);
    }
    
    /**
     * 절감액 계산
     */
    public void calculateSavings(Long originalPrice) {
        long originalTotal = originalPrice * quantity;
        this.savingsAmount = originalTotal - totalProductAmount;
    }
}
