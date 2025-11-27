package com.example.payflow.catalog.presentation;

import com.example.payflow.catalog.application.CatalogOrderService;
import com.example.payflow.catalog.presentation.dto.CreateOrderRequest;
import com.example.payflow.catalog.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
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
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        log.info("주문 목록 조회: 매장={}", storeId);
        List<OrderResponse> orders = orderService.getStoreOrders(storeId);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 내 주문 목록 조회 (유통업체별)
     * GET /api/orders/my/distributor/{distributorId}
     */
    @GetMapping("/my/distributor/{distributorId}")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<OrderResponse>> getMyOrdersByDistributor(
            @PathVariable String distributorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        log.info("주문 목록 조회: 매장={}, 유통업체={}", storeId, distributorId);
        List<OrderResponse> orders = orderService.getStoreOrdersByDistributor(storeId, distributorId);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 주문 상세 조회
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
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
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
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
    
    /**
     * 유통업체에 들어온 주문 목록 조회
     * GET /api/catalog-orders/distributor
     */
    @GetMapping("/distributor")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<List<OrderResponse>> getDistributorOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        log.info("유통업체 주문 목록 조회: 유통업체={}", distributorId);
        List<OrderResponse> orders = orderService.getDistributorOrders(distributorId);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 주문 확정 (결제 완료 후)
     * POST /api/catalog-orders/{orderId}/confirm
     */
    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        log.info("주문 확정 요청: 매장={}, 주문ID={}", storeId, orderId);
        
        try {
            OrderResponse order = orderService.confirmOrder(orderId, storeId);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.error("주문 확정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
