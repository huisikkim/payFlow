package com.example.payflow.crypto.application;

import com.example.payflow.crypto.domain.CandleData;
import com.example.payflow.crypto.domain.RSICalculator;
import com.example.payflow.crypto.domain.VolumeSurgeCalculator;
import com.example.payflow.crypto.infrastructure.BithumbCandleClient;
import com.example.payflow.crypto.infrastructure.UpbitCandleClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * RSI 계산 및 캐싱 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RSIService {
    
    private final UpbitCandleClient upbitCandleClient;
    private final BithumbCandleClient bithumbCandleClient;
    
    // RSI 캐시 (market -> RSI)
    private final Map<String, BigDecimal> upbitRSICache = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> bithumbRSICache = new ConcurrentHashMap<>();
    
    // 거래량 급증률 캐시 (market -> surge %)
    private final Map<String, BigDecimal> upbitVolumeSurgeCache = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> bithumbVolumeSurgeCache = new ConcurrentHashMap<>();
    
    // 지원하는 마켓 목록
    private static final List<String> MARKETS = List.of(
        "KRW-BTC", "KRW-ETH", "KRW-XRP", "KRW-ADA", "KRW-SOL",
        "KRW-DOGE", "KRW-AVAX", "KRW-MATIC", "KRW-DOT", "KRW-SHIB"
    );
    
    // 빗썸 심볼 매핑
    private static final Map<String, String> BITHUMB_SYMBOLS = Map.of(
        "KRW-BTC", "BTC",
        "KRW-ETH", "ETH",
        "KRW-XRP", "XRP",
        "KRW-ADA", "ADA",
        "KRW-SOL", "SOL",
        "KRW-DOGE", "DOGE",
        "KRW-AVAX", "AVAX",
        "KRW-MATIC", "MATIC",
        "KRW-DOT", "DOT",
        "KRW-SHIB", "SHIB"
    );
    
    @PostConstruct
    public void init() {
        log.info("🚀 RSI 서비스 초기화 시작");
        updateAllRSI();
    }
    
    /**
     * 1분마다 RSI 업데이트
     */
    @Scheduled(fixedRate = 60000) // 60초
    public void updateAllRSI() {
        log.info("📊 RSI 업데이트 시작");
        
        MARKETS.forEach(market -> {
            try {
                // 업비트 RSI 계산
                updateUpbitRSI(market);
                
                // 빗썸 RSI 계산
                String bithumbSymbol = BITHUMB_SYMBOLS.get(market);
                if (bithumbSymbol != null) {
                    updateBithumbRSI(market, bithumbSymbol);
                }
                
                Thread.sleep(100); // API 호출 간격
            } catch (Exception e) {
                log.error("❌ RSI 업데이트 실패: {}", market, e);
            }
        });
        
        log.info("✅ RSI 업데이트 완료 - 업비트: {}, 빗썸: {}", 
                upbitRSICache.size(), bithumbRSICache.size());
    }
    
    private void updateUpbitRSI(String market) {
        try {
            List<CandleData> candles = upbitCandleClient.getMinuteCandles(market, 30);
            
            if (candles.size() >= 15) {
                // RSI 계산
                List<BigDecimal> closePrices = candles.stream()
                        .map(CandleData::getClosePrice)
                        .collect(Collectors.toList());
                
                BigDecimal rsi = RSICalculator.calculate(closePrices);
                upbitRSICache.put(market, rsi);
                
                // 거래량 급증률 계산 (최근 10개 캔들 기준)
                if (candles.size() >= 11) {
                    List<BigDecimal> volumes = candles.subList(candles.size() - 11, candles.size() - 1)
                            .stream()
                            .map(CandleData::getVolume)
                            .collect(Collectors.toList());
                    
                    BigDecimal currentVolume = candles.get(candles.size() - 1).getVolume();
                    BigDecimal volumeSurge = VolumeSurgeCalculator.calculate(volumes, currentVolume);
                    upbitVolumeSurgeCache.put(market, volumeSurge);
                    
                    log.debug("📈 업비트 RSI: {} = {}, 거래량: {}%", market, rsi, volumeSurge);
                }
            }
        } catch (Exception e) {
            log.error("❌ 업비트 RSI 계산 실패: {}", market, e);
        }
    }
    
    private void updateBithumbRSI(String market, String symbol) {
        try {
            List<CandleData> candles = bithumbCandleClient.getMinuteCandles(symbol, 30);
            
            if (candles.size() >= 15) {
                // RSI 계산
                List<BigDecimal> closePrices = candles.stream()
                        .map(CandleData::getClosePrice)
                        .collect(Collectors.toList());
                
                BigDecimal rsi = RSICalculator.calculate(closePrices);
                bithumbRSICache.put(market, rsi);
                
                // 거래량 급증률 계산 (최근 10개 캔들 기준)
                if (candles.size() >= 11) {
                    List<BigDecimal> volumes = candles.subList(candles.size() - 11, candles.size() - 1)
                            .stream()
                            .map(CandleData::getVolume)
                            .collect(Collectors.toList());
                    
                    BigDecimal currentVolume = candles.get(candles.size() - 1).getVolume();
                    BigDecimal volumeSurge = VolumeSurgeCalculator.calculate(volumes, currentVolume);
                    bithumbVolumeSurgeCache.put(market, volumeSurge);
                    
                    log.debug("📈 빗썸 RSI: {} = {}, 거래량: {}%", market, rsi, volumeSurge);
                }
            }
        } catch (Exception e) {
            log.error("❌ 빗썸 RSI 계산 실패: {}", market, e);
        }
    }
    
    /**
     * 업비트 RSI 조회
     */
    public BigDecimal getUpbitRSI(String market) {
        return upbitRSICache.getOrDefault(market, BigDecimal.ZERO);
    }
    
    /**
     * 빗썸 RSI 조회
     */
    public BigDecimal getBithumbRSI(String market) {
        return bithumbRSICache.getOrDefault(market, BigDecimal.ZERO);
    }
    
    /**
     * 업비트 거래량 급증률 조회
     */
    public BigDecimal getUpbitVolumeSurge(String market) {
        return upbitVolumeSurgeCache.getOrDefault(market, BigDecimal.valueOf(100));
    }
    
    /**
     * 빗썸 거래량 급증률 조회
     */
    public BigDecimal getBithumbVolumeSurge(String market) {
        return bithumbVolumeSurgeCache.getOrDefault(market, BigDecimal.valueOf(100));
    }
    
    /**
     * 모든 RSI 데이터 조회
     */
    public Map<String, Map<String, BigDecimal>> getAllRSI() {
        Map<String, Map<String, BigDecimal>> result = new HashMap<>();
        
        MARKETS.forEach(market -> {
            Map<String, BigDecimal> rsiData = new HashMap<>();
            rsiData.put("upbit", getUpbitRSI(market));
            rsiData.put("bithumb", getBithumbRSI(market));
            result.put(market, rsiData);
        });
        
        return result;
    }
    
    /**
     * 모든 지표 데이터 조회 (RSI + 거래량 급증률)
     */
    public Map<String, Map<String, Object>> getAllIndicators() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        
        MARKETS.forEach(market -> {
            Map<String, Object> indicators = new HashMap<>();
            
            // 업비트 데이터
            Map<String, Object> upbitData = new HashMap<>();
            BigDecimal upbitRSI = getUpbitRSI(market);
            BigDecimal upbitVolume = getUpbitVolumeSurge(market);
            upbitData.put("rsi", upbitRSI);
            upbitData.put("volumeSurge", upbitVolume);
            upbitData.put("rsiStatus", RSICalculator.getRSIStatus(upbitRSI));
            upbitData.put("volumeStatus", VolumeSurgeCalculator.getVolumeStatus(upbitVolume));
            upbitData.put("signalStrength", VolumeSurgeCalculator.getSignalStrength(upbitRSI, upbitVolume));
            
            // 빗썸 데이터
            Map<String, Object> bithumbData = new HashMap<>();
            BigDecimal bithumbRSI = getBithumbRSI(market);
            BigDecimal bithumbVolume = getBithumbVolumeSurge(market);
            bithumbData.put("rsi", bithumbRSI);
            bithumbData.put("volumeSurge", bithumbVolume);
            bithumbData.put("rsiStatus", RSICalculator.getRSIStatus(bithumbRSI));
            bithumbData.put("volumeStatus", VolumeSurgeCalculator.getVolumeStatus(bithumbVolume));
            bithumbData.put("signalStrength", VolumeSurgeCalculator.getSignalStrength(bithumbRSI, bithumbVolume));
            
            indicators.put("upbit", upbitData);
            indicators.put("bithumb", bithumbData);
            result.put(market, indicators);
        });
        
        return result;
    }
}
