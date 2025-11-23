package com.example.payflow.distributor.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.ingredientorder.domain.*;
import com.example.payflow.ingredientorder.domain.event.IngredientOrderConfirmedEvent;
import com.example.payflow.ingredientorder.domain.event.IngredientOrderRejectedEvent;
import com.example.payflow.logging.application.EventLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributorOrderService {
    
    private final IngredientOrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    private final EventLoggingService eventLoggingService;
    
    @Transactional(readOnly = true)
    public List<IngredientOrder> getPendingOrders(String distributorId) {
        return orderRepository.findByDistributorIdAndStatus(distributorId, IngredientOrderStatus.PENDING);
    }
    
    @Transactional(readOnly = true)
    public List<IngredientOrder> getOrdersByDistributor(String distributorId) {
        return orderRepository.findByDistributorId(distributorId);
    }
    
    @Transactional
    public void confirmOrder(String orderId) {
        IngredientOrder order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다: " + orderId));
        
        order.confirm();
        orderRepository.save(order);
        
        log.info("✅ 유통사 발주 확인: orderId={}, distributorId={}", orderId, order.getDistributorId());
        
        // Kafka 이벤트 발행
        IngredientOrderConfirmedEvent event = new IngredientOrderConfirmedEvent(
            orderId,
            order.getStoreId(),
            order.getDistributorId(),
            order.getTotalAmount()
        );
        eventPublisher.publish(event);
        
        // 이벤트 로그
        eventLoggingService.logEvent(
            orderId,
            "IngredientOrderConfirmed",
            "distributor-order",
            Map.of(
                "orderId", orderId,
                "distributorId", order.getDistributorId(),
                "storeId", order.getStoreId(),
                "totalAmount", order.getTotalAmount()
            )
        );
    }
    
    @Transactional
    public void rejectOrder(String orderId, String reason) {
        IngredientOrder order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다: " + orderId));
        
        order.reject(reason);
        orderRepository.save(order);
        
        log.info("❌ 유통사 발주 거절: orderId={}, reason={}", orderId, reason);
        
        // Kafka 이벤트 발행
        IngredientOrderRejectedEvent event = new IngredientOrderRejectedEvent(
            orderId,
            order.getStoreId(),
            order.getDistributorId(),
            reason
        );
        eventPublisher.publish(event);
        
        // 이벤트 로그
        eventLoggingService.logEvent(
            orderId,
            "IngredientOrderRejected",
            "distributor-order",
            Map.of(
                "orderId", orderId,
                "distributorId", order.getDistributorId(),
                "reason", reason
            )
        );
    }
    
    @Transactional
    public void updateItemPrice(String orderId, Long itemId, Long newPrice) {
        IngredientOrder order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다: " + orderId));
        
        if (order.getStatus() != IngredientOrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 발주만 단가를 수정할 수 있습니다.");
        }
        
        order.updateItemPrice(itemId, newPrice);
        orderRepository.save(order);
        
        log.info("💰 발주 품목 단가 수정: orderId={}, itemId={}, newPrice={}", orderId, itemId, newPrice);
        
        // 이벤트 로그
        eventLoggingService.logEvent(
            orderId,
            "ItemPriceUpdated",
            "distributor-order",
            Map.of(
                "orderId", orderId,
                "itemId", itemId,
                "newPrice", newPrice,
                "newTotalAmount", order.getTotalAmount()
            )
        );
    }
}
