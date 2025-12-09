package com.example.payflow.crypto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 체결강도 데이터 모델
 * 매수/매도 비율을 통해 시장 심리 파악
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeStrength {
    private String exchange;            // 거래소명
    private String market;              // 마켓 코드
    
    // 1분 데이터
    private BigDecimal buyVolume1m;     // 1분 매수 거래량
    private BigDecimal sellVolume1m;    // 1분 매도 거래량
    private BigDecimal totalVolume1m;   // 1분 총 거래량
    private BigDecimal strength1m;      // 1분 체결강도 (%)
    private String direction1m;         // BUY_DOMINANT, SELL_DOMINANT, NEUTRAL
    
    // 5분 데이터
    private BigDecimal buyVolume5m;     // 5분 매수 거래량
    private BigDecimal sellVolume5m;    // 5분 매도 거래량
    private BigDecimal totalVolume5m;   // 5분 총 거래량
    private BigDecimal strength5m;      // 5분 체결강도 (%)
    private String direction5m;         // BUY_DOMINANT, SELL_DOMINANT, NEUTRAL
    
    // 거래량 변화율
    private BigDecimal volumeChangeRate1m;  // 1분 거래량 변화율 (%)
    private BigDecimal volumeChangeRate5m;  // 5분 거래량 변화율 (%)
    
    private LocalDateTime timestamp;
}
