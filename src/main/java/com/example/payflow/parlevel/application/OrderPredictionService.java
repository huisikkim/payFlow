package com.example.payflow.parlevel.application;

import com.example.payflow.inventory.domain.Inventory;
import com.example.payflow.inventory.domain.InventoryRepository;
import com.example.payflow.parlevel.domain.*;
import com.example.payflow.parlevel.presentation.dto.PredictedOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPredictionService {
    
    private final ParLevelRepository parLevelRepository;
    private final PredictedOrderRepository predictedOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final ConsumptionAnalysisService consumptionAnalysisService;
    
    @Transactional
    public List<PredictedOrderResponse> generatePredictions(String storeId) {
        List<ParLevel> parLevels = parLevelRepository.findByStoreId(storeId);
        List<PredictedOrder> predictions = new ArrayList<>();
        
        for (ParLevel parLevel : parLevels) {
            // 현재 재고 조회
            Inventory inventory = inventoryRepository.findByProductId(parLevel.getItemName())
                .orElse(null);
            
            if (inventory == null) {
                log.warn("⚠️ 재고 정보 없음: itemName={}", parLevel.getItemName());
                continue;
            }
            
            int currentStock = inventory.getQuantity() - inventory.getReservedQuantity();
            
            // 리드타임 동안 예상 소비량
            int predictedConsumption = consumptionAnalysisService.predictConsumption(
                storeId, parLevel.getItemName(), parLevel.getLeadTimeDays()
            );
            
            // 재발주 필요 여부 확인
            int projectedStock = currentStock - predictedConsumption;
            
            if (projectedStock <= parLevel.getMinLevel()) {
                int orderQuantity = parLevel.calculateOrderQuantity(currentStock);
                
                String reason = String.format(
                    "현재 재고: %d, 예상 소비: %d, 예상 잔여: %d (최소: %d)",
                    currentStock, predictedConsumption, projectedStock, parLevel.getMinLevel()
                );
                
                PredictedOrder prediction = new PredictedOrder(
                    storeId,
                    parLevel.getItemName(),
                    currentStock,
                    predictedConsumption,
                    orderQuantity,
                    LocalDate.now(),
                    LocalDate.now().plusDays(parLevel.getLeadTimeDays()),
                    reason
                );
                
                predictions.add(prediction);
                predictedOrderRepository.save(prediction);
                
                log.info("🔮 발주 예측 생성: itemName={}, currentStock={}, orderQty={}", 
                    parLevel.getItemName(), currentStock, orderQuantity);
            }
        }
        
        return predictions.stream()
            .map(PredictedOrderResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PredictedOrderResponse> getPendingPredictions(String storeId) {
        return predictedOrderRepository.findByStoreIdAndStatus(storeId, PredictionStatus.PENDING)
            .stream()
            .map(PredictedOrderResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PredictedOrderResponse> getAllPredictions(String storeId) {
        return predictedOrderRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
            .stream()
            .map(PredictedOrderResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void markAsOrdered(Long predictionId, String orderId) {
        PredictedOrder prediction = predictedOrderRepository.findById(predictionId)
            .orElseThrow(() -> new IllegalArgumentException("예측을 찾을 수 없습니다: " + predictionId));
        
        prediction.markAsOrdered(orderId);
        log.info("✅ 예측 발주 완료: predictionId={}, orderId={}", predictionId, orderId);
    }
    
    @Transactional
    public void markAsSkipped(Long predictionId) {
        PredictedOrder prediction = predictedOrderRepository.findById(predictionId)
            .orElseThrow(() -> new IllegalArgumentException("예측을 찾을 수 없습니다: " + predictionId));
        
        prediction.markAsSkipped();
        log.info("⏭️ 예측 건너뜀: predictionId={}", predictionId);
    }
}
