package com.example.payflow.pricelearning.presentation;

import com.example.payflow.pricelearning.application.PriceAlertService;
import com.example.payflow.pricelearning.application.PriceLearningService;
import com.example.payflow.pricelearning.domain.ItemPriceHistory;
import com.example.payflow.pricelearning.domain.PriceAlert;
import com.example.payflow.pricelearning.domain.PriceStatistics;
import com.example.payflow.pricelearning.presentation.dto.PriceAlertResponse;
import com.example.payflow.pricelearning.presentation.dto.PriceHistoryResponse;
import com.example.payflow.pricelearning.presentation.dto.PriceStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/price-learning")
@RequiredArgsConstructor
public class PriceLearningController {
    
    private final PriceLearningService priceLearningService;
    private final PriceAlertService priceAlertService;
    
    /**
     * 품목별 단가 통계 조회
     */
    @GetMapping("/items/{itemName}/statistics")
    public ResponseEntity<PriceStatisticsResponse> getStatistics(
            @PathVariable String itemName,
            @RequestParam(defaultValue = "30") int days) {
        
        PriceStatistics stats = priceLearningService.getStatistics(itemName, days);
        return ResponseEntity.ok(PriceStatisticsResponse.from(stats));
    }
    
    /**
     * 품목별 단가 이력 조회
     */
    @GetMapping("/items/{itemName}/history")
    public ResponseEntity<List<PriceHistoryResponse>> getPriceHistory(
            @PathVariable String itemName,
            @RequestParam(defaultValue = "30") int days) {
        
        List<ItemPriceHistory> histories = priceLearningService.getPriceHistory(itemName, days);
        List<PriceHistoryResponse> responses = histories.stream()
            .map(PriceHistoryResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 추천 단가 조회
     */
    @GetMapping("/items/{itemName}/recommended-price")
    public ResponseEntity<Long> getRecommendedPrice(@PathVariable String itemName) {
        Long recommendedPrice = priceLearningService.calculateRecommendedPrice(itemName);
        return ResponseEntity.ok(recommendedPrice);
    }
    
    /**
     * 모든 품목 목록 조회
     */
    @GetMapping("/items")
    public ResponseEntity<List<String>> getAllItems() {
        List<String> items = priceLearningService.getAllItemNames();
        return ResponseEntity.ok(items);
    }
    
    /**
     * 활성 경고 목록 조회
     */
    @GetMapping("/alerts/active")
    public ResponseEntity<List<PriceAlertResponse>> getActiveAlerts() {
        List<PriceAlert> alerts = priceAlertService.getActiveAlerts();
        List<PriceAlertResponse> responses = alerts.stream()
            .map(PriceAlertResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 최근 경고 목록 조회
     */
    @GetMapping("/alerts/recent")
    public ResponseEntity<List<PriceAlertResponse>> getRecentAlerts() {
        List<PriceAlert> alerts = priceAlertService.getRecentAlerts();
        List<PriceAlertResponse> responses = alerts.stream()
            .map(PriceAlertResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 경고 상세 조회
     */
    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<PriceAlertResponse> getAlert(@PathVariable String alertId) {
        PriceAlert alert = priceAlertService.getAlert(alertId);
        return ResponseEntity.ok(PriceAlertResponse.from(alert));
    }
    
    /**
     * 경고 확인 처리
     */
    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable String alertId) {
        priceAlertService.acknowledgeAlert(alertId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 경고 해결 처리
     */
    @PostMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable String alertId) {
        priceAlertService.resolveAlert(alertId);
        return ResponseEntity.ok().build();
    }
}
