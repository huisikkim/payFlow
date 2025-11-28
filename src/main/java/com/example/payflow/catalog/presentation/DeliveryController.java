package com.example.payflow.catalog.presentation;

import com.example.payflow.catalog.application.DeliveryService;
import com.example.payflow.catalog.domain.DeliveryStatus;
import com.example.payflow.catalog.presentation.dto.DeliveryResponse;
import com.example.payflow.catalog.presentation.dto.StartShippingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {
    
    private final DeliveryService deliveryService;
    
    /**
     * 배송 정보 생성 (주문 확정 후)
     * POST /api/deliveries/order/{orderId}
     */
    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<DeliveryResponse> createDeliveryInfo(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        log.info("배송 정보 생성 요청: 유통업체={}, 주문ID={}", distributorId, orderId);
        
        try {
            DeliveryResponse delivery = deliveryService.createDeliveryInfo(orderId);
            return ResponseEntity.ok(delivery);
        } catch (IllegalArgumentException e) {
            log.error("배송 정보 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 배송 시작 (유통업자)
     * POST /api/deliveries/order/{orderId}/ship
     */
    @PostMapping("/order/{orderId}/ship")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<DeliveryResponse> startShipping(
            @PathVariable Long orderId,
            @RequestBody StartShippingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        log.info("배송 시작 요청: 유통업체={}, 주문ID={}, 송장번호={}", 
                distributorId, orderId, request.getTrackingNumber());
        
        try {
            DeliveryResponse delivery = deliveryService.startShipping(orderId, request);
            return ResponseEntity.ok(delivery);
        } catch (IllegalArgumentException e) {
            log.error("배송 시작 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 배송 완료 (유통업자)
     * POST /api/deliveries/order/{orderId}/complete
     */
    @PostMapping("/order/{orderId}/complete")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<DeliveryResponse> completeDelivery(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        log.info("배송 완료 요청: 유통업체={}, 주문ID={}", distributorId, orderId);
        
        try {
            DeliveryResponse delivery = deliveryService.completeDelivery(orderId);
            return ResponseEntity.ok(delivery);
        } catch (IllegalArgumentException e) {
            log.error("배송 완료 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 배송 정보 조회 (주문 ID로)
     * GET /api/deliveries/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(@PathVariable Long orderId) {
        log.info("배송 정보 조회: 주문ID={}", orderId);
        
        try {
            DeliveryResponse delivery = deliveryService.getDeliveryByOrderId(orderId);
            return ResponseEntity.ok(delivery);
        } catch (IllegalArgumentException e) {
            log.error("배송 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 유통업자별 배송 목록 조회
     * GET /api/deliveries/distributor
     */
    @GetMapping("/distributor")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<List<DeliveryResponse>> getDistributorDeliveries() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        log.info("유통업체 배송 목록 조회: 유통업체={}", distributorId);
        
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByDistributor(distributorId);
        return ResponseEntity.ok(deliveries);
    }
    
    /**
     * 매장별 배송 목록 조회
     * GET /api/deliveries/store
     */
    @GetMapping("/store")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<DeliveryResponse>> getStoreDeliveries() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        log.info("매장 배송 목록 조회: 매장={}", storeId);
        
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByStore(storeId);
        return ResponseEntity.ok(deliveries);
    }
    
    /**
     * 상태별 배송 목록 조회
     * GET /api/deliveries/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByStatus(@PathVariable DeliveryStatus status) {
        log.info("상태별 배송 목록 조회: 상태={}", status);
        
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByStatus(status);
        return ResponseEntity.ok(deliveries);
    }
}
