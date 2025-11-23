package com.example.payflow.parlevel.presentation;

import com.example.payflow.ingredientorder.presentation.dto.IngredientOrderResponse;
import com.example.payflow.parlevel.application.AutoOrderService;
import com.example.payflow.parlevel.application.ConsumptionAnalysisService;
import com.example.payflow.parlevel.application.OrderPredictionService;
import com.example.payflow.parlevel.application.ParLevelService;
import com.example.payflow.parlevel.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parlevel")
@RequiredArgsConstructor
public class ParLevelController {
    
    private final ParLevelService parLevelService;
    private final OrderPredictionService orderPredictionService;
    private final AutoOrderService autoOrderService;
    private final ConsumptionAnalysisService consumptionAnalysisService;
    
    // Par Level 관리
    @PostMapping("/settings")
    public ResponseEntity<ParLevelResponse> createParLevel(@RequestBody CreateParLevelRequest request) {
        return ResponseEntity.ok(parLevelService.createParLevel(request));
    }
    
    @PutMapping("/settings/{id}")
    public ResponseEntity<ParLevelResponse> updateParLevel(
            @PathVariable Long id,
            @RequestBody UpdateParLevelRequest request) {
        return ResponseEntity.ok(parLevelService.updateParLevel(id, request));
    }
    
    @PostMapping("/settings/{id}/enable-auto-order")
    public ResponseEntity<Void> enableAutoOrder(@PathVariable Long id) {
        parLevelService.enableAutoOrder(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/settings/{id}/disable-auto-order")
    public ResponseEntity<Void> disableAutoOrder(@PathVariable Long id) {
        parLevelService.disableAutoOrder(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/settings/{storeId}")
    public ResponseEntity<List<ParLevelResponse>> getParLevelsByStore(@PathVariable String storeId) {
        return ResponseEntity.ok(parLevelService.getParLevelsByStore(storeId));
    }
    
    @GetMapping("/settings/{storeId}/{itemName}")
    public ResponseEntity<ParLevelResponse> getParLevel(
            @PathVariable String storeId,
            @PathVariable String itemName) {
        return ResponseEntity.ok(parLevelService.getParLevel(storeId, itemName));
    }
    
    // Par Level 자동 계산
    @PostMapping("/settings/auto-calculate")
    public ResponseEntity<ParLevelResponse> autoCalculateParLevel(
            @RequestParam String storeId,
            @RequestParam String itemName,
            @RequestParam String unit,
            @RequestParam Integer leadTimeDays) {
        return ResponseEntity.ok(parLevelService.autoCalculateParLevel(storeId, itemName, unit, leadTimeDays));
    }
    
    // 발주 예측
    @PostMapping("/predictions/{storeId}/generate")
    public ResponseEntity<List<PredictedOrderResponse>> generatePredictions(@PathVariable String storeId) {
        return ResponseEntity.ok(orderPredictionService.generatePredictions(storeId));
    }
    
    @GetMapping("/predictions/{storeId}/pending")
    public ResponseEntity<List<PredictedOrderResponse>> getPendingPredictions(@PathVariable String storeId) {
        return ResponseEntity.ok(orderPredictionService.getPendingPredictions(storeId));
    }
    
    @GetMapping("/predictions/{storeId}")
    public ResponseEntity<List<PredictedOrderResponse>> getAllPredictions(@PathVariable String storeId) {
        return ResponseEntity.ok(orderPredictionService.getAllPredictions(storeId));
    }
    
    @PostMapping("/predictions/{predictionId}/skip")
    public ResponseEntity<Void> skipPrediction(@PathVariable Long predictionId) {
        orderPredictionService.markAsSkipped(predictionId);
        return ResponseEntity.ok().build();
    }
    
    // 자동 발주
    @PostMapping("/auto-order/{predictionId}")
    public ResponseEntity<IngredientOrderResponse> executeAutoOrder(
            @PathVariable Long predictionId,
            @RequestParam String distributorId) {
        return ResponseEntity.ok(autoOrderService.executeAutoOrder(predictionId, distributorId));
    }
    
    @PostMapping("/auto-order/{storeId}/execute-all")
    public ResponseEntity<List<IngredientOrderResponse>> executeAllAutoOrders(
            @PathVariable String storeId,
            @RequestParam String distributorId) {
        return ResponseEntity.ok(autoOrderService.executeAllAutoOrders(storeId, distributorId));
    }
    
    // 소비 패턴 분석
    @GetMapping("/consumption/{storeId}/{itemName}")
    public ResponseEntity<List<ConsumptionPatternResponse>> getConsumptionHistory(
            @PathVariable String storeId,
            @PathVariable String itemName,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(
            consumptionAnalysisService.getConsumptionHistory(storeId, itemName, days)
                .stream()
                .map(ConsumptionPatternResponse::from)
                .collect(Collectors.toList())
        );
    }
    
    @GetMapping("/consumption/{storeId}/{itemName}/statistics")
    public ResponseEntity<Map<String, Object>> getConsumptionStatistics(
            @PathVariable String storeId,
            @PathVariable String itemName,
            @RequestParam(defaultValue = "30") int days) {
        
        Double avgDaily = consumptionAnalysisService.calculateAverageDailyConsumption(storeId, itemName, days);
        Double stdDev = consumptionAnalysisService.calculateStandardDeviation(storeId, itemName, days);
        Integer predicted7Days = consumptionAnalysisService.predictConsumption(storeId, itemName, 7);
        
        return ResponseEntity.ok(Map.of(
            "averageDailyConsumption", avgDaily,
            "standardDeviation", stdDev,
            "predicted7DaysConsumption", predicted7Days,
            "analysisWindow", days + " days"
        ));
    }
}
