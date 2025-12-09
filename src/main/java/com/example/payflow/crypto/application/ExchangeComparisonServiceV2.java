package com.example.payflow.crypto.application;

import com.example.payflow.crypto.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 거래소 비교 서비스 V2
 * - 프리미엄 계산
 * - VWAP 계산
 * - 체결강도 분석
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeComparisonServiceV2 {
    
    private final UpbitWebSocketService upbitService;
    private final BithumbWebSocketService bithumbService;
    
    // 체결강도 계산을 위한 거래량 히스토리 (거래소별, 마켓별)
    private final Map<String, List<VolumeSnapshot>> volumeHistory = new ConcurrentHashMap<>();
    
    /**
     * 모든 코인의 거래소 간 프리미엄 비교
     */
    public List<ExchangePremium> getAllPremiums(String referenceExchange) {
        List<ExchangePremium> premiums = new ArrayList<>();
        
        List<CoinTicker> upbitTickers = upbitService.getAllTickers();
        
        for (CoinTicker upbitTicker : upbitTickers) {
            ExchangePremium premium = calculatePremium(upbitTicker.getMarket(), referenceExchange);
            if (premium != null) {
                premiums.add(premium);
            }
        }
        
        // 프리미엄 절대값 기준 정렬
        premiums.sort((a, b) -> b.getMaxPremium().abs().compareTo(a.getMaxPremium().abs()));
        
        return premiums;
    }
    
    /**
     * 특정 코인의 거래소 간 프리미엄 계산
     */
    public ExchangePremium calculatePremium(String market, String referenceExchange) {
        CoinTicker upbitTicker = upbitService.getTicker(market);
        CoinTicker bithumbTicker = bithumbService.getTicker(market);
        
        if (upbitTicker == null || bithumbTicker == null) {
            return null;
        }
        
        // 기준 가격 설정
        BigDecimal referencePrice = "UPBIT".equals(referenceExchange) 
            ? upbitTicker.getTradePrice() 
            : bithumbTicker.getTradePrice();
        
        // 거래소별 가격 및 프리미엄 계산
        Map<String, ExchangePremium.ExchangePrice> exchangePrices = new HashMap<>();
        
        // 업비트
        BigDecimal upbitPremium = calculatePremiumPercent(upbitTicker.getTradePrice(), referencePrice);
        BigDecimal upbitSpread = upbitTicker.getTradePrice().subtract(referencePrice);
        exchangePrices.put("UPBIT", ExchangePremium.ExchangePrice.builder()
            .exchange("UPBIT")
            .price(upbitTicker.getTradePrice())
            .premium(upbitPremium)
            .spread(upbitSpread)
            .volume24h(upbitTicker.getAccTradePrice24h())
            .changeDirection(upbitTicker.getChange())
            .changeRate(upbitTicker.getSignedChangeRate())
            .build());
        
        // 빗썸
        BigDecimal bithumbPremium = calculatePremiumPercent(bithumbTicker.getTradePrice(), referencePrice);
        BigDecimal bithumbSpread = bithumbTicker.getTradePrice().subtract(referencePrice);
        exchangePrices.put("BITHUMB", ExchangePremium.ExchangePrice.builder()
            .exchange("BITHUMB")
            .price(bithumbTicker.getTradePrice())
            .premium(bithumbPremium)
            .spread(bithumbSpread)
            .volume24h(bithumbTicker.getAccTradePrice24h())
            .changeDirection(bithumbTicker.getChange())
            .changeRate(bithumbTicker.getSignedChangeRate())
            .build());
        
        // VWAP 계산 (거래량 가중 평균 가격)
        BigDecimal vwap = calculateVWAP(
            upbitTicker.getTradePrice(), upbitTicker.getAccTradePrice24h(),
            bithumbTicker.getTradePrice(), bithumbTicker.getAccTradePrice24h()
        );
        BigDecimal vwapPremium = calculatePremiumPercent(vwap, referencePrice);
        
        // 최대/최소 프리미엄 찾기
        BigDecimal maxPremium = upbitPremium.compareTo(bithumbPremium) > 0 ? upbitPremium : bithumbPremium;
        BigDecimal minPremium = upbitPremium.compareTo(bithumbPremium) < 0 ? upbitPremium : bithumbPremium;
        String maxPremiumExchange = upbitPremium.compareTo(bithumbPremium) > 0 ? "UPBIT" : "BITHUMB";
        String minPremiumExchange = upbitPremium.compareTo(bithumbPremium) < 0 ? "UPBIT" : "BITHUMB";
        
        return ExchangePremium.builder()
            .market(market)
            .koreanName(upbitTicker.getKoreanName())
            .referenceExchange(referenceExchange)
            .referencePrice(referencePrice)
            .exchangePrices(exchangePrices)
            .vwap(vwap)
            .vwapPremium(vwapPremium)
            .maxPremium(maxPremium)
            .minPremium(minPremium)
            .maxPremiumExchange(maxPremiumExchange)
            .minPremiumExchange(minPremiumExchange)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * 프리미엄 % 계산
     */
    private BigDecimal calculatePremiumPercent(BigDecimal targetPrice, BigDecimal referencePrice) {
        if (referencePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return targetPrice.subtract(referencePrice)
            .divide(referencePrice, 6, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * VWAP (Volume Weighted Average Price) 계산
     */
    private BigDecimal calculateVWAP(BigDecimal price1, BigDecimal volume1, 
                                      BigDecimal price2, BigDecimal volume2) {
        BigDecimal totalVolume = volume1.add(volume2);
        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
            return price1.add(price2).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
        
        BigDecimal weightedSum = price1.multiply(volume1).add(price2.multiply(volume2));
        return weightedSum.divide(totalVolume, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 체결강도 계산 (매수/매도 비율)
     * 실제로는 체결 데이터가 필요하지만, 여기서는 거래량 변화로 추정
     */
    public TradeStrength calculateTradeStrength(String exchange, String market) {
        String key = exchange + ":" + market;
        
        // 현재 거래량 스냅샷 저장
        CoinTicker ticker = "UPBIT".equals(exchange) 
            ? upbitService.getTicker(market) 
            : bithumbService.getTicker(market);
        
        if (ticker == null) {
            return null;
        }
        
        VolumeSnapshot currentSnapshot = VolumeSnapshot.builder()
            .timestamp(LocalDateTime.now())
            .volume(ticker.getAccTradeVolume24h())
            .build();
        
        // 히스토리에 추가
        volumeHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(currentSnapshot);
        
        // 오래된 데이터 제거 (10분 이상)
        List<VolumeSnapshot> history = volumeHistory.get(key);
        history.removeIf(s -> s.getTimestamp().isBefore(LocalDateTime.now().minusMinutes(10)));
        
        // 1분, 5분 거래량 변화율 계산
        BigDecimal volumeChange1m = calculateVolumeChange(history, 1);
        BigDecimal volumeChange5m = calculateVolumeChange(history, 5);
        
        // 체결강도 추정 (거래량 증가 = 매수 우위로 가정)
        BigDecimal strength1m = BigDecimal.valueOf(100).add(volumeChange1m);
        BigDecimal strength5m = BigDecimal.valueOf(100).add(volumeChange5m);
        
        String direction1m = getDirection(strength1m);
        String direction5m = getDirection(strength5m);
        
        return TradeStrength.builder()
            .exchange(exchange)
            .market(market)
            .strength1m(strength1m)
            .direction1m(direction1m)
            .strength5m(strength5m)
            .direction5m(direction5m)
            .volumeChangeRate1m(volumeChange1m)
            .volumeChangeRate5m(volumeChange5m)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private BigDecimal calculateVolumeChange(List<VolumeSnapshot> history, int minutes) {
        if (history.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        VolumeSnapshot recent = history.get(history.size() - 1);
        
        Optional<VolumeSnapshot> oldSnapshot = history.stream()
            .filter(s -> s.getTimestamp().isBefore(cutoff))
            .reduce((first, second) -> second); // 가장 최근 것
        
        if (oldSnapshot.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal oldVolume = oldSnapshot.get().getVolume();
        BigDecimal newVolume = recent.getVolume();
        
        if (oldVolume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return newVolume.subtract(oldVolume)
            .divide(oldVolume, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    private String getDirection(BigDecimal strength) {
        if (strength.compareTo(BigDecimal.valueOf(120)) > 0) {
            return "BUY_DOMINANT";
        } else if (strength.compareTo(BigDecimal.valueOf(80)) < 0) {
            return "SELL_DOMINANT";
        } else {
            return "NEUTRAL";
        }
    }
    
    /**
     * 차익거래 기회 찾기
     */
    public List<ExchangePremium> findArbitrageOpportunities(String referenceExchange, BigDecimal minPremium) {
        List<ExchangePremium> opportunities = new ArrayList<>();
        List<ExchangePremium> allPremiums = getAllPremiums(referenceExchange);
        
        for (ExchangePremium premium : allPremiums) {
            if (premium.getMaxPremium().abs().compareTo(minPremium) >= 0) {
                opportunities.add(premium);
            }
        }
        
        return opportunities;
    }
    
    // 거래량 스냅샷 내부 클래스
    @lombok.Data
    @lombok.Builder
    private static class VolumeSnapshot {
        private LocalDateTime timestamp;
        private BigDecimal volume;
    }
}
