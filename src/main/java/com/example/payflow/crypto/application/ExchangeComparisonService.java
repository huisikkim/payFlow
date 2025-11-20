package com.example.payflow.crypto.application;

import com.example.payflow.crypto.domain.CoinTicker;
import com.example.payflow.crypto.domain.ExchangeComparison;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeComparisonService {
    
    private final UpbitWebSocketService upbitService;
    private final BithumbWebSocketService bithumbService;
    
    /**
     * 거래소 간 가격 비교 데이터 생성
     */
    public List<ExchangeComparison> getComparisons() {
        List<ExchangeComparison> comparisons = new ArrayList<>();
        
        List<CoinTicker> upbitTickers = upbitService.getAllTickers();
        
        for (CoinTicker upbitTicker : upbitTickers) {
            CoinTicker bithumbTicker = bithumbService.getTicker(upbitTicker.getMarket());
            
            if (bithumbTicker != null) {
                ExchangeComparison comparison = createComparison(upbitTicker, bithumbTicker);
                comparisons.add(comparison);
            }
        }
        
        // 가격 차이가 큰 순으로 정렬
        comparisons.sort((a, b) -> b.getPriceDiffPercent().abs().compareTo(a.getPriceDiffPercent().abs()));
        
        return comparisons;
    }
    
    /**
     * 특정 코인의 거래소 간 비교
     */
    public ExchangeComparison getComparison(String market) {
        CoinTicker upbitTicker = upbitService.getTicker(market);
        CoinTicker bithumbTicker = bithumbService.getTicker(market);
        
        if (upbitTicker != null && bithumbTicker != null) {
            return createComparison(upbitTicker, bithumbTicker);
        }
        
        return null;
    }
    
    /**
     * 차익거래 기회 찾기 (가격 차이 1% 이상)
     */
    public List<ExchangeComparison> getArbitrageOpportunities(BigDecimal minDiffPercent) {
        List<ExchangeComparison> opportunities = new ArrayList<>();
        
        List<ExchangeComparison> comparisons = getComparisons();
        
        for (ExchangeComparison comparison : comparisons) {
            if (comparison.getPriceDiffPercent().abs().compareTo(minDiffPercent) >= 0) {
                opportunities.add(comparison);
            }
        }
        
        return opportunities;
    }
    
    private ExchangeComparison createComparison(CoinTicker upbitTicker, CoinTicker bithumbTicker) {
        BigDecimal upbitPrice = upbitTicker.getTradePrice();
        BigDecimal bithumbPrice = bithumbTicker.getTradePrice();
        
        // 가격 차이 계산
        BigDecimal priceDiff = upbitPrice.subtract(bithumbPrice);
        
        // 가격 차이 퍼센트 계산 (업비트 기준)
        BigDecimal priceDiffPercent = upbitPrice.compareTo(BigDecimal.ZERO) != 0
            ? priceDiff.divide(upbitPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        // 거래량 비교
        BigDecimal upbitVolume = upbitTicker.getAccTradePrice24h();
        BigDecimal bithumbVolume = bithumbTicker.getAccTradePrice24h();
        BigDecimal totalVolume = upbitVolume.add(bithumbVolume);
        
        // 어느 거래소가 더 비싼지
        String cheaperExchange = priceDiff.compareTo(BigDecimal.ZERO) > 0 ? "BITHUMB" : "UPBIT";
        String expensiveExchange = priceDiff.compareTo(BigDecimal.ZERO) > 0 ? "UPBIT" : "BITHUMB";
        
        return ExchangeComparison.builder()
            .market(upbitTicker.getMarket())
            .koreanName(upbitTicker.getKoreanName())
            .upbitPrice(upbitPrice)
            .bithumbPrice(bithumbPrice)
            .priceDiff(priceDiff.abs())
            .priceDiffPercent(priceDiffPercent)
            .upbitVolume(upbitVolume)
            .bithumbVolume(bithumbVolume)
            .totalVolume(totalVolume)
            .cheaperExchange(cheaperExchange)
            .expensiveExchange(expensiveExchange)
            .upbitChange(upbitTicker.getChange())
            .bithumbChange(bithumbTicker.getChange())
            .build();
    }
}
