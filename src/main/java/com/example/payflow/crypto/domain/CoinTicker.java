package com.example.payflow.crypto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinTicker {
    private String market;              // 마켓 코드 (KRW-BTC)
    private String koreanName;          // 한글명
    private BigDecimal tradePrice;      // 현재가
    private BigDecimal changePrice;     // 전일 대비 가격 변화
    private BigDecimal changeRate;      // 전일 대비 변화율
    private BigDecimal signedChangeRate; // 부호가 있는 변화율
    private BigDecimal highPrice;       // 고가
    private BigDecimal lowPrice;        // 저가
    private BigDecimal openingPrice;    // 시가
    private BigDecimal prevClosingPrice; // 전일 종가
    private BigDecimal accTradePrice24h; // 24시간 누적 거래대금
    private BigDecimal accTradeVolume24h; // 24시간 누적 거래량
    private String change;              // RISE, FALL, EVEN
    private LocalDateTime timestamp;    // 타임스탬프
    private BigDecimal rsi;             // RSI 지표 (0-100)
    private String rsiStatus;           // RSI 상태 (OVERBOUGHT, OVERSOLD, NEUTRAL)
    private BigDecimal volumeSurge;     // 거래량 급증률 (%)
    private String volumeStatus;        // 거래량 상태 (SURGE, NORMAL, LOW)
}
