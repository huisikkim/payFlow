package com.example.payflow.saga.application;

import com.example.payflow.inventory.domain.*;
import com.example.payflow.order.domain.Order;
import com.example.payflow.order.domain.OrderRepository;
import com.example.payflow.payment.domain.Payment;
import com.example.payflow.payment.domain.PaymentRepository;
import com.example.payflow.saga.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {
    
    private final OrderSagaRepository sagaRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    
    @Transactional
    public String startOrderSaga(String orderId) {
        String sagaId = UUID.randomUUID().toString();
        OrderSaga saga = new OrderSaga(sagaId, orderId);
        sagaRepository.save(saga);
        
        log.info("🚀 Saga 시작: sagaId={}, orderId={}", sagaId, orderId);
        return sagaId;
    }
    
    @Transactional
    public void processPayment(String sagaId, String paymentKey) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Saga를 찾을 수 없습니다."));
        
        try {
            saga.moveToPaymentProcessed(paymentKey);
            sagaRepository.save(saga);
            log.info("✅ 결제 처리 완료: sagaId={}, paymentKey={}", sagaId, paymentKey);
        } catch (Exception e) {
            log.error("❌ 결제 처리 실패: sagaId={}", sagaId, e);
            compensate(saga, "결제 처리 실패: " + e.getMessage());
            throw e;
        }
    }
    
    public void reserveInventory(String sagaId, String productId, Integer quantity) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Saga를 찾을 수 없습니다."));
        
        try {
            reserveInventoryInternal(saga, productId, quantity);
        } catch (Exception e) {
            log.error("❌ 재고 예약 실패: sagaId={}", sagaId, e);
            compensate(saga, "재고 예약 실패: " + e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    private void reserveInventoryInternal(OrderSaga saga, String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        if (!inventory.canReserve(quantity)) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        
        inventory.reserve(quantity);
        inventoryRepository.save(inventory);
        
        InventoryReservation reservation = new InventoryReservation(saga.getOrderId(), productId, quantity);
        reservationRepository.save(reservation);
        
        saga.moveToInventoryReserved(reservation.getId());
        sagaRepository.save(saga);
        
        log.info("✅ 재고 예약 완료: sagaId={}, productId={}, quantity={}", saga.getSagaId(), productId, quantity);
    }
    
    @Transactional
    public void completeSaga(String sagaId) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Saga를 찾을 수 없습니다."));
        
        saga.complete();
        sagaRepository.save(saga);
        
        log.info("🎉 Saga 완료: sagaId={}", sagaId);
    }
    
    @Transactional
    public void compensate(OrderSaga saga, String errorMessage) {
        log.warn("🔄 보상 트랜잭션 시작: sagaId={}, error={}", saga.getSagaId(), errorMessage);
        
        saga.startCompensation(errorMessage);
        sagaRepository.save(saga);
        
        try {
            // Step 3: 재고 예약 취소
            if (saga.getInventoryReservationId() != null) {
                compensateInventoryReservation(saga);
            }
            
            // Step 2: 결제 취소
            if (saga.getPaymentKey() != null) {
                compensatePayment(saga);
            }
            
            // Step 1: 주문 취소
            compensateOrder(saga);
            
            saga.compensated();
            sagaRepository.save(saga);
            
            log.info("✅ 보상 트랜잭션 완료: sagaId={}", saga.getSagaId());
        } catch (Exception e) {
            log.error("❌ 보상 트랜잭션 실패: sagaId={}", saga.getSagaId(), e);
            saga.fail("보상 실패: " + e.getMessage());
            sagaRepository.save(saga);
        }
    }
    
    private void compensateInventoryReservation(OrderSaga saga) {
        InventoryReservation reservation = reservationRepository.findById(saga.getInventoryReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        
        Inventory inventory = inventoryRepository.findByProductId(reservation.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        inventory.cancelReservation(reservation.getQuantity());
        inventoryRepository.save(inventory);
        
        reservation.cancel();
        reservationRepository.save(reservation);
        
        log.info("↩️ 재고 예약 취소 완료: reservationId={}", reservation.getId());
    }
    
    private void compensatePayment(OrderSaga saga) {
        Payment payment = paymentRepository.findByOrderId(saga.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다."));
        
        payment.cancel();
        paymentRepository.save(payment);
        
        log.info("↩️ 결제 취소 완료: orderId={}", saga.getOrderId());
    }
    
    private void compensateOrder(OrderSaga saga) {
        Order order = orderRepository.findByOrderId(saga.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        order.cancel();
        orderRepository.save(order);
        
        log.info("↩️ 주문 취소 완료: orderId={}", saga.getOrderId());
    }
}
