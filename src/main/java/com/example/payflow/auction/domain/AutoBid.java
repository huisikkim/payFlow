package com.example.payflow.auction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auto_bids")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoBid {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long auctionId;
    
    @Column(nullable = false)
    private String bidderId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal maxAmount;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public boolean canBid(BigDecimal requiredAmount) {
        return isActive && maxAmount.compareTo(requiredAmount) >= 0;
    }
}
