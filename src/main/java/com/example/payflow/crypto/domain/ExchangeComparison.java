package com.example.payflow.crypto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeComparison {
    private String market;              // 마켓 코드 (KRW-BTC)
    private String koreanName;          // 한글명
    
    // 가격 정보
    private BigDecimal upbitPrice;      // 업비트 가격
    private BigDecimal bithumbPrice;    // 빗썸 가격
    private BigDecimal priceDiff;       // 가격 차이 (절대값)
    private BigDecimal priceDiffPercent; // 가격 차이 퍼센트
    
    // 거래량 정보
    private BigDecimal upbitVolume;     // 업비트 24시간 거래대금
    private BigDecimal bithumbVolume;   // 빗썸 24시간 거래대금
    private BigDecimal totalVolume;     // 총 거래대금
    
    // 차익거래 정보
    private String cheaperExchange;     // 더 저렴한 거래소
    private String expensiveExchange;   // 더 비싼 거래소
    
    // 변동 정보
    private String upbitChange;         // 업비트 변동 (RISE/FALL/EVEN)
    private String bithumbChange;       // 빗썸 변동 (RISE/FALL/EVEN)
}
