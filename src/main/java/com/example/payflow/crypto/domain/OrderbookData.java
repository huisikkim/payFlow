package com.example.payflow.crypto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 표준화된 호가 데이터 모델
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderbookData {
    private String exchange;            // 거래소명
    private String market;              // 마켓 코드
    
    // 최우선 호가
    private BigDecimal bestBidPrice;    // 최우선 매수호가
    private BigDecimal bestAskPrice;    // 최우선 매도호가
    private BigDecimal spread;          // 스프레드 (매도-매수)
    private BigDecimal spreadPercent;   // 스프레드 비율 (%)
    
    // 호가 리스트 (상위 10개)
    private List<OrderbookUnit> bids;   // 매수 호가
    private List<OrderbookUnit> asks;   // 매도 호가
    
    private LocalDateTime timestamp;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderbookUnit {
        private BigDecimal price;       // 호가
        private BigDecimal quantity;    // 수량
        private BigDecimal total;       // 총액
    }
}
