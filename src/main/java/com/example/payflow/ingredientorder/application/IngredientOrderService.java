package com.example.payflow.ingredientorder.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.ingredientorder.domain.*;
import com.example.payflow.ingredientorder.domain.event.IngredientOrderCreatedEvent;
import com.example.payflow.ingredientorder.presentation.dto.CreateIngredientOrderRequest;
import com.example.payflow.ingredientorder.presentation.dto.IngredientOrderResponse;
import com.example.payflow.logging.application.EventLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientOrderService {
    
    private final IngredientOrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final EventLoggingService eventLoggingService;
    
    @Transactional
    public IngredientOrderResponse createOrder(CreateIngredientOrderRequest request) {
        long startTime = System.currentTimeMillis();
        String orderId = "INGR_ORDER_" + UUID.randomUUID().toString().substring(0, 8);
        
        IngredientOrder order = new IngredientOrder(
            orderId,
            request.getStoreId(),
            request.getDistributorId()
        );
        
        // 발주 품목 추가
        request.getItems().forEach(itemDto -> {
            IngredientOrderItem item = new IngredientOrderItem(
                itemDto.getItemName(),
                itemDto.getQuantity(),
                itemDto.getUnitPrice(),
                itemDto.getUnit()
            );
            order.addItem(item);
        });
        
        orderRepository.save(order);
        log.info("✅ 식자재 발주 생성: orderId={}, storeId={}, distributorId={}, totalAmount={}", 
            orderId, request.getStoreId(), request.getDistributorId(), order.getTotalAmount());
        
        // Kafka 이벤트 발행
        IngredientOrderCreatedEvent event = new IngredientOrderCreatedEvent(
            orderId,
            request.getStoreId(),
            request.getDistributorId(),
            order.getTotalAmount()
        );
        eventPublisher.publish(event);
        
        // 이벤트 로그 저장
        long processingTime = System.currentTimeMillis() - startTime;
        eventLoggingService.logEvent(
            orderId,
            "IngredientOrderCreated",
            "ingredient-order",
            Map.of(
                "orderId", orderId,
                "storeId", request.getStoreId(),
                "distributorId", request.getDistributorId(),
                "totalAmount", order.getTotalAmount(),
                "itemCount", order.getItems().size(),
                "processingTimeMs", processingTime
            )
        );
        
        return IngredientOrderResponse.from(order);
    }
    
    @Transactional(readOnly = true)
    public IngredientOrderResponse getOrder(String orderId) {
        IngredientOrder order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다: " + orderId));
        return IngredientOrderResponse.from(order);
    }
    
    @Transactional(readOnly = true)
    public List<IngredientOrderResponse> getOrdersByStore(String storeId) {
        return orderRepository.findByStoreId(storeId).stream()
            .map(IngredientOrderResponse::from)
            .collect(Collectors.toList());
    }
}
