package com.example.payflow.sourcing.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "keyword_analysis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String keyword;
    
    @Column(nullable = false)
    private String platform; // NAVER, COUPANG
    
    private Long searchVolume; // 월간 검색량
    
    private Integer competitionScore; // 경쟁도 (0-100)
    
    private Integer productCount; // 상품 수
    
    private Double averagePrice; // 평균 가격
    
    private Double profitabilityScore; // 수익성 점수 (검색량/경쟁도 비율)
    
    @Column(length = 20)
    private String marketType; // BLUE_OCEAN, RED_OCEAN, NORMAL
    
    private LocalDateTime analyzedAt;
    
    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }
}
