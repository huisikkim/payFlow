package com.example.payflow.auction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private String sellerId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal startPrice;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal buyNowPrice;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal minBidIncrement = new BigDecimal("1000");
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuctionStatus status;
    
    private String winnerId;
    
    private Long winningBidId;
    
    @Builder.Default
    private Integer bidCount = 0;
    
    @Builder.Default
    private Integer viewCount = 0;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentPrice == null) {
            currentPrice = startPrice;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 비즈니스 로직
    public void placeBid(BigDecimal bidAmount, String bidderId, Long bidId) {
        if (status != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("경매가 진행 중이 아닙니다.");
        }
        
        if (LocalDateTime.now().isAfter(endTime)) {
            throw new IllegalStateException("경매가 종료되었습니다.");
        }
        
        if (bidderId.equals(sellerId)) {
            throw new IllegalArgumentException("본인의 경매에는 입찰할 수 없습니다.");
        }
        
        BigDecimal minBidAmount = currentPrice.add(minBidIncrement);
        if (bidAmount.compareTo(minBidAmount) < 0) {
            throw new IllegalArgumentException(
                String.format("입찰 금액은 최소 %s원 이상이어야 합니다.", minBidAmount)
            );
        }
        
        this.currentPrice = bidAmount;
        this.bidCount++;
        this.winnerId = bidderId;
        this.winningBidId = bidId;
    }
    
    public void buyNow(String buyerId, Long bidId) {
        if (buyNowPrice == null) {
            throw new IllegalStateException("즉시 구매가가 설정되지 않았습니다.");
        }
        
        if (status != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("경매가 진행 중이 아닙니다.");
        }
        
        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("본인의 경매는 즉시 구매할 수 없습니다.");
        }
        
        this.currentPrice = buyNowPrice;
        this.winnerId = buyerId;
        this.winningBidId = bidId;
        this.status = AuctionStatus.ENDED;
    }
    
    public void end() {
        if (status != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("진행 중인 경매만 종료할 수 있습니다.");
        }
        this.status = AuctionStatus.ENDED;
    }
    
    public void cancel() {
        if (bidCount > 0) {
            throw new IllegalStateException("입찰이 있는 경매는 취소할 수 없습니다.");
        }
        this.status = AuctionStatus.CANCELLED;
    }
    
    public void start() {
        if (status != AuctionStatus.SCHEDULED) {
            throw new IllegalStateException("예정된 경매만 시작할 수 있습니다.");
        }
        this.status = AuctionStatus.ACTIVE;
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public boolean isActive() {
        return status == AuctionStatus.ACTIVE && LocalDateTime.now().isBefore(endTime);
    }
    
    public boolean hasWinner() {
        return winnerId != null;
    }
}
