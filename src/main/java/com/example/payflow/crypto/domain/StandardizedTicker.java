package com.example.payflow.crypto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 표준화된 실시간 체결 데이터 모델
 * 모든 거래소의 데이터를 통일된 형식으로 변환
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardizedTicker {
    // 기본 정보
    private String exchange;            // 거래소명 (UPBIT, BITHUMB)
    private String market;              // 마켓 코드 (KRW-BTC)
    private String symbol;              // 심볼 (BTC)
    private String koreanName;          // 한글명
    
    // 가격 정보
    private BigDecimal currentPrice;    // 현재가
    private BigDecimal openPrice;       // 시가
    private BigDecimal highPrice;       // 고가
    private BigDecimal lowPrice;        // 저가
    private BigDecimal prevClosePrice;  // 전일 종가
    
    // 변동 정보
    private BigDecimal changePrice;     // 전일 대비 가격 변화
    private BigDecimal changeRate;      // 전일 대비 변화율 (%)
    private String changeDirection;     // RISE, FALL, EVEN
    
    // 거래량 정보
    private BigDecimal volume24h;       // 24시간 거래량 (코인 수량)
    private BigDecimal volumeValue24h;  // 24시간 거래대금 (원)
    
    // 타임스탬프
    private LocalDateTime timestamp;
    private long timestampMillis;
}
