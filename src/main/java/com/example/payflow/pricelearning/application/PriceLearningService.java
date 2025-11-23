package com.example.payflow.pricelearning.application;

import com.example.payflow.pricelearning.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceLearningService {
    
    private final ItemPriceHistoryRepository priceHistoryRepository;
    
    @Transactional
    public void recordPrice(String itemName, Long unitPrice, String unit,
                           String orderId, String distributorId, String storeId) {
        ItemPriceHistory history = new ItemPriceHistory(
            itemName, unitPrice, unit, orderId, distributorId, storeId
        );
        priceHistoryRepository.save(history);
        log.info("📊 단가 이력 저장: itemName={}, unitPrice={}, orderId={}", 
            itemName, unitPrice, orderId);
    }
    
    @Transactional(readOnly = true)
    public PriceStatistics getStatistics(String itemName, int days) {
        LocalDateTime after = LocalDateTime.now().minusDays(days);
        
        List<ItemPriceHistory> histories = priceHistoryRepository
            .findByItemNameAndRecordedAtAfterOrderByRecordedAtDesc(itemName, after);
        
        if (histories.isEmpty()) {
            return new PriceStatistics(itemName, 0L, 0L, 0L, 0L, 0L, 0, 0.0);
        }
        
        // 통계 계산
        Long avgPrice = Math.round(histories.stream()
            .mapToLong(ItemPriceHistory::getUnitPrice)
            .average()
            .orElse(0.0));
        
        Long minPrice = histories.stream()
            .mapToLong(ItemPriceHistory::getUnitPrice)
            .min()
            .orElse(0L);
        
        Long maxPrice = histories.stream()
            .mapToLong(ItemPriceHistory::getUnitPrice)
            .max()
            .orElse(0L);
        
        Long recentPrice = histories.get(0).getUnitPrice();
        
        // 변동성 계산 (표준편차 / 평균)
        double variance = histories.stream()
            .mapToDouble(h -> Math.pow(h.getUnitPrice() - avgPrice, 2))
            .average()
            .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        double volatility = avgPrice > 0 ? (stdDev / avgPrice) * 100 : 0.0;
        
        // 추천 단가 (최근 7일 가중 평균)
        Long recommendedPrice = calculateRecommendedPrice(itemName);
        
        return new PriceStatistics(
            itemName, avgPrice, minPrice, maxPrice, recentPrice,
            recommendedPrice, histories.size(), volatility
        );
    }
    
    @Transactional(readOnly = true)
    public Long calculateRecommendedPrice(String itemName) {
        LocalDateTime recent7Days = LocalDateTime.now().minusDays(7);
        LocalDateTime recent30Days = LocalDateTime.now().minusDays(30);
        
        Double recentAvg = priceHistoryRepository.calculateAveragePrice(itemName, recent7Days);
        Double monthlyAvg = priceHistoryRepository.calculateAveragePrice(itemName, recent30Days);
        
        if (recentAvg == null && monthlyAvg == null) {
            return 0L;
        }
        
        if (recentAvg == null) {
            return Math.round(monthlyAvg);
        }
        
        if (monthlyAvg == null) {
            return Math.round(recentAvg);
        }
        
        // 최근 7일 70%, 30일 평균 30% 가중치
        return Math.round(recentAvg * 0.7 + monthlyAvg * 0.3);
    }
    
    @Transactional(readOnly = true)
    public List<ItemPriceHistory> getPriceHistory(String itemName, int days) {
        LocalDateTime after = LocalDateTime.now().minusDays(days);
        return priceHistoryRepository
            .findByItemNameAndRecordedAtAfterOrderByRecordedAtDesc(itemName, after);
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllItemNames() {
        return priceHistoryRepository.findAllDistinctItemNames();
    }
}
