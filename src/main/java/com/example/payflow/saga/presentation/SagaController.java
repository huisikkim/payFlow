package com.example.payflow.saga.presentation;

import com.example.payflow.inventory.application.InventoryService;
import com.example.payflow.inventory.domain.Inventory;
import com.example.payflow.order.application.OrderService;
import com.example.payflow.order.domain.Order;
import com.example.payflow.saga.application.OrderSagaOrchestrator;
import com.example.payflow.saga.domain.OrderSaga;
import com.example.payflow.saga.domain.OrderSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/saga")
@RequiredArgsConstructor
@Slf4j
public class SagaController {
    
    private final OrderSagaOrchestrator sagaOrchestrator;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final OrderSagaRepository sagaRepository;
    private final com.example.payflow.payment.domain.PaymentRepository paymentRepository;
    
    @PostMapping("/test/success")
    public ResponseEntity<Map<String, Object>> testSuccessScenario(@RequestBody TestOrderRequest request) {
        log.info("=== Saga 성공 시나리오 테스트 시작 ===");
        
        try {
            // 1. 재고 확인 (이미 존재하는 재고 사용)
            Inventory inventory = inventoryService.getInventory(request.getProductId());
            log.info("재고 확인: productId={}, quantity={}", inventory.getProductId(), inventory.getQuantity());
            
            // 2. 주문 생성
            Order order = orderService.createOrder(
                    request.getProductName(),
                    request.getAmount(),
                    request.getCustomerEmail(),
                    request.getCustomerName()
            );
            log.info("주문 생성: orderId={}", order.getOrderId());
            
            // 3. Saga 시작
            String sagaId = sagaOrchestrator.startOrderSaga(order.getOrderId());
            
            // 4. 결제 생성 및 처리
            com.example.payflow.payment.domain.Payment payment = new com.example.payflow.payment.domain.Payment(
                    order.getOrderId(),
                    order.getOrderName(),
                    order.getAmount(),
                    order.getCustomerEmail()
            );
            paymentRepository.save(payment);
            
            String mockPaymentKey = "test_payment_" + System.currentTimeMillis();
            payment.approve(mockPaymentKey, "CARD");
            paymentRepository.save(payment);
            
            sagaOrchestrator.processPayment(sagaId, mockPaymentKey);
            
            // 5. 재고 예약
            sagaOrchestrator.reserveInventory(sagaId, request.getProductId(), 1);
            
            // 6. Saga 완료
            sagaOrchestrator.completeSaga(sagaId);
            
            OrderSaga saga = sagaRepository.findBySagaId(sagaId).orElseThrow();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sagaId", sagaId);
            response.put("orderId", order.getOrderId());
            response.put("sagaStatus", saga.getStatus());
            response.put("currentStep", saga.getCurrentStep());
            response.put("message", "✅ Saga가 성공적으로 완료되었습니다!");
            
            log.info("=== Saga 성공 시나리오 테스트 완료 ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Saga 테스트 실패", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/test/failure")
    public ResponseEntity<Map<String, Object>> testFailureScenario(@RequestBody TestOrderRequest request) {
        log.info("=== Saga 실패 시나리오 테스트 시작 (보상 트랜잭션) ===");
        
        try {
            // 1. 재고 확인 (재고 0인 상품 사용)
            Inventory inventory = inventoryService.getInventory(request.getProductId());
            log.info("재고 확인: productId={}, quantity={}", inventory.getProductId(), inventory.getQuantity());
            
            // 2. 주문 생성
            Order order = orderService.createOrder(
                    request.getProductName(),
                    request.getAmount(),
                    request.getCustomerEmail(),
                    request.getCustomerName()
            );
            log.info("주문 생성: orderId={}", order.getOrderId());
            
            // 3. Saga 시작
            String sagaId = sagaOrchestrator.startOrderSaga(order.getOrderId());
            
            // 4. 결제 생성 및 처리
            com.example.payflow.payment.domain.Payment payment = new com.example.payflow.payment.domain.Payment(
                    order.getOrderId(),
                    order.getOrderName(),
                    order.getAmount(),
                    order.getCustomerEmail()
            );
            paymentRepository.save(payment);
            
            String mockPaymentKey = "test_payment_" + System.currentTimeMillis();
            payment.approve(mockPaymentKey, "CARD");
            paymentRepository.save(payment);
            
            sagaOrchestrator.processPayment(sagaId, mockPaymentKey);
            
            // 5. 재고 예약 (실패 예상 - 재고 부족)
            try {
                sagaOrchestrator.reserveInventory(sagaId, request.getProductId(), 1);
            } catch (Exception e) {
                log.info("예상된 실패 발생: {}", e.getMessage());
                // 보상 트랜잭션은 이미 OrderSagaOrchestrator에서 실행됨
            }
            
            OrderSaga saga = sagaRepository.findBySagaId(sagaId).orElseThrow();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sagaId", sagaId);
            response.put("orderId", order.getOrderId());
            response.put("sagaStatus", saga.getStatus());
            response.put("currentStep", saga.getCurrentStep());
            response.put("errorMessage", saga.getErrorMessage());
            response.put("message", "✅ 보상 트랜잭션이 성공적으로 실행되었습니다!");
            
            log.info("=== Saga 실패 시나리오 테스트 완료 ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Saga 테스트 실패", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/status/{sagaId}")
    public ResponseEntity<Map<String, Object>> getSagaStatus(@PathVariable String sagaId) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Saga를 찾을 수 없습니다."));
        
        Map<String, Object> response = new HashMap<>();
        response.put("sagaId", saga.getSagaId());
        response.put("orderId", saga.getOrderId());
        response.put("status", saga.getStatus());
        response.put("currentStep", saga.getCurrentStep());
        response.put("paymentKey", saga.getPaymentKey());
        response.put("inventoryReservationId", saga.getInventoryReservationId());
        response.put("errorMessage", saga.getErrorMessage());
        response.put("createdAt", saga.getCreatedAt());
        response.put("updatedAt", saga.getUpdatedAt());
        
        return ResponseEntity.ok(response);
    }
}
