package com.example.payflow.crypto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 캔들 데이터 (OHLCV)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleData {
    private String market;              // 마켓 코드
    private LocalDateTime timestamp;    // 캔들 시간
    private BigDecimal openPrice;       // 시가
    private BigDecimal highPrice;       // 고가
    private BigDecimal lowPrice;        // 저가
    private BigDecimal closePrice;      // 종가
    private BigDecimal volume;          // 거래량
}
