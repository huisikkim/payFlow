package com.example.payflow.parlevel.application;

import com.example.payflow.ingredientorder.application.IngredientOrderService;
import com.example.payflow.ingredientorder.presentation.dto.CreateIngredientOrderRequest;
import com.example.payflow.ingredientorder.presentation.dto.IngredientOrderItemDto;
import com.example.payflow.ingredientorder.presentation.dto.IngredientOrderResponse;
import com.example.payflow.parlevel.domain.PredictedOrder;
import com.example.payflow.parlevel.domain.PredictedOrderRepository;
import com.example.payflow.parlevel.domain.PredictionStatus;
import com.example.payflow.pricelearning.application.PriceLearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoOrderService {
    
    private final PredictedOrderRepository predictedOrderRepository;
    private final IngredientOrderService ingredientOrderService;
    private final PriceLearningService priceLearningService;
    private final OrderPredictionService orderPredictionService;
    
    @Transactional
    public IngredientOrderResponse executeAutoOrder(Long predictionId, String distributorId) {
        PredictedOrder prediction = predictedOrderRepository.findById(predictionId)
            .orElseThrow(() -> new IllegalArgumentException("예측을 찾을 수 없습니다: " + predictionId));
        
        if (prediction.getStatus() != PredictionStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 예측입니다: " + predictionId);
        }
        
        // 최적 단가 조회
        Long recommendedPrice = priceLearningService.calculateRecommendedPrice(prediction.getItemName());
        
        // 발주 생성
        IngredientOrderItemDto itemDto = new IngredientOrderItemDto(
            prediction.getItemName(),
            prediction.getRecommendedOrderQuantity(),
            recommendedPrice,
            "kg"
        );
        
        CreateIngredientOrderRequest orderRequest = new CreateIngredientOrderRequest(
            prediction.getStoreId(),
            distributorId,
            List.of(itemDto)
        );
        
        IngredientOrderResponse order = ingredientOrderService.createOrder(orderRequest);
        
        // 예측 상태 업데이트
        orderPredictionService.markAsOrdered(predictionId, order.getOrderId());
        
        log.info("🤖 자동 발주 실행: predictionId={}, orderId={}, itemName={}, quantity={}", 
            predictionId, order.getOrderId(), prediction.getItemName(), 
            prediction.getRecommendedOrderQuantity());
        
        return order;
    }
    
    @Transactional
    public List<IngredientOrderResponse> executeAllAutoOrders(String storeId, String distributorId) {
        List<PredictedOrder> predictions = predictedOrderRepository
            .findByStoreIdAndStatus(storeId, PredictionStatus.PENDING);
        
        return predictions.stream()
            .map(prediction -> executeAutoOrder(prediction.getId(), distributorId))
            .toList();
    }
}
