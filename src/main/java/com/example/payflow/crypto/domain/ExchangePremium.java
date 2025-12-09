package com.example.payflow.crypto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 거래소 간 프리미엄 비교 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangePremium {
    private String market;              // 마켓 코드
    private String koreanName;          // 한글명
    
    // 기준 거래소
    private String referenceExchange;   // 기준 거래소 (UPBIT or BITHUMB)
    private BigDecimal referencePrice;  // 기준 가격
    
    // 거래소별 가격 및 프리미엄
    private Map<String, ExchangePrice> exchangePrices;
    
    // VWAP (거래량 가중 평균 가격)
    private BigDecimal vwap;            // 전체 거래소 VWAP
    private BigDecimal vwapPremium;     // VWAP 대비 기준가 프리미엄 (%)
    
    // 최대/최소 프리미엄
    private BigDecimal maxPremium;      // 최대 프리미엄 (%)
    private BigDecimal minPremium;      // 최소 프리미엄 (%)
    private String maxPremiumExchange;  // 최대 프리미엄 거래소
    private String minPremiumExchange;  // 최소 프리미엄 거래소
    
    private LocalDateTime timestamp;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExchangePrice {
        private String exchange;            // 거래소명
        private BigDecimal price;           // 현재가
        private BigDecimal premium;         // 기준가 대비 프리미엄 (%)
        private BigDecimal spread;          // 기준가와의 가격 차이 (원)
        private BigDecimal volume24h;       // 24시간 거래대금
        private String changeDirection;     // RISE, FALL, EVEN
        private BigDecimal changeRate;      // 변동률 (%)
    }
}
