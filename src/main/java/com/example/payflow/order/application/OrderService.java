package com.example.payflow.order.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.order.domain.Order;
import com.example.payflow.order.domain.OrderRepository;
import com.example.payflow.order.domain.event.OrderCreatedEvent;
import com.example.payflow.order.presentation.dto.CreateOrderRequest;
import com.example.payflow.order.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String orderId = "ORDER_" + UUID.randomUUID().toString().substring(0, 8);
        
        Order order = new Order(
            orderId,
            request.getOrderName(),
            request.getAmount(),
            request.getCustomerEmail(),
            request.getCustomerName()
        );
        
        orderRepository.save(order);
        log.info("✅ 주문 생성: {}", orderId);
        
        // 이벤트 발행 (Kafka로 전송)
        OrderCreatedEvent event = new OrderCreatedEvent(
            orderId,
            request.getOrderName(),
            request.getAmount(),
            request.getCustomerEmail()
        );
        eventPublisher.publish(event);
        
        return OrderResponse.from(order);
    }
    
    @Transactional
    public void confirmOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        order.confirm();
        log.info("주문 확정: {}", orderId);
    }
    
    @Transactional
    public void failOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        order.fail();
        log.info("주문 실패: {}", orderId);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        return OrderResponse.from(order);
    }
}
