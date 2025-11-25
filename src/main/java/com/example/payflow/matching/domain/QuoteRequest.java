package com.example.payflow.matching.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quote_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String storeName;
    
    @Column(nullable = false)
    private String distributorId;
    
    @Column(nullable = false)
    private String distributorName;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestedProducts;  // 요청 품목 (콤마 구분)
    
    @Column(columnDefinition = "TEXT")
    private String message;  // 추가 요청사항
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuoteStatus status;  // 견적 상태
    
    @Column
    private Integer estimatedAmount;  // 예상 금액
    
    @Column(columnDefinition = "TEXT")
    private String distributorResponse;  // 유통업체 응답
    
    @Column
    private LocalDateTime requestedAt;  // 요청 시간
    
    @Column
    private LocalDateTime respondedAt;  // 응답 시간
    
    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        if (status == null) {
            status = QuoteStatus.PENDING;
        }
    }
    
    public enum QuoteStatus {
        PENDING,    // 대기중
        ACCEPTED,   // 수락됨
        REJECTED,   // 거절됨
        COMPLETED   // 완료됨
    }
}
