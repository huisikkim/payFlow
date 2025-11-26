package com.example.payflow.catalog.presentation;

import com.example.payflow.catalog.application.CatalogOrderService;
import com.example.payflow.catalog.presentation.dto.CreateOrderRequest;
import com.example.payflow.catalog.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog-orders")
@RequiredArgsConstructor
@Slf4j
public class CatalogOrderController {
    
    private final CatalogOrderService orderService;
    
    /**
     * 장바구니에서 주문 생성
     * POST /api/orders/create
     */
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-Store-Id") String storeId,
            @RequestBody CreateOrderRequest request) {
        
        log.info("주문 생성 요청: 매장={}, 유통업체={}", storeId, request.getDistributorId());
        
        try {
            OrderResponse order = orderService.createOrderFromCart(storeId, request);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.error("주문 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 내 주문 목록 조회 (전체)
     * GET /api/orders/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestHeader("X-Store-Id") String storeId) {
        
        log.info("주문 목록 조회: 매장={}", storeId);
        List<OrderResponse> orders = orderService.getStoreOrders(storeId);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 내 주문 목록 조회 (유통업체별)
     * GET /api/orders/my/distributor/{distributorId}
     */
    @GetMapping("/my/distributor/{distributorId}")
    public ResponseEntity<List<OrderResponse>> getMyOrdersByDistributor(
            @RequestHeader("X-Store-Id") String storeId,
            @PathVariable String distributorId) {
        
        log.info("주문 목록 조회: 매장={}, 유통업체={}", storeId, distributorId);
        List<OrderResponse> orders = orderService.getStoreOrdersByDistributor(storeId, distributorId);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 주문 상세 조회
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @RequestHeader("X-Store-Id") String storeId,
            @PathVariable Long orderId) {
        
        log.info("주문 상세 조회: 매장={}, 주문ID={}", storeId, orderId);
        
        try {
            OrderResponse order = orderService.getOrderDetail(orderId, storeId);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.error("주문 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 주문 취소
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestHeader("X-Store-Id") String storeId,
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body) {
        
        String reason = body != null ? body.getOrDefault("reason", "고객 요청") : "고객 요청";
        log.info("주문 취소 요청: 매장={}, 주문ID={}, 사유={}", storeId, orderId, reason);
        
        try {
            OrderResponse order = orderService.cancelOrder(orderId, storeId, reason);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.error("주문 취소 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
