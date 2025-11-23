package com.example.payflow.distributor.presentation;

import com.example.payflow.distributor.application.DistributorOrderService;
import com.example.payflow.distributor.presentation.dto.RejectOrderRequest;
import com.example.payflow.distributor.presentation.dto.UpdateItemPriceRequest;
import com.example.payflow.ingredientorder.domain.IngredientOrder;
import com.example.payflow.ingredientorder.presentation.dto.IngredientOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/distributor/orders")
@RequiredArgsConstructor
@Slf4j
public class DistributorOrderController {
    
    private final DistributorOrderService distributorOrderService;
    
    @GetMapping
    public ResponseEntity<List<IngredientOrderResponse>> getOrders(@RequestParam String distributorId) {
        List<IngredientOrder> orders = distributorOrderService.getOrdersByDistributor(distributorId);
        List<IngredientOrderResponse> responses = orders.stream()
            .map(IngredientOrderResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<IngredientOrderResponse>> getPendingOrders(@RequestParam String distributorId) {
        List<IngredientOrder> orders = distributorOrderService.getPendingOrders(distributorId);
        List<IngredientOrderResponse> responses = orders.stream()
            .map(IngredientOrderResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable String orderId) {
        log.info("✅ 유통사 발주 확인 요청: orderId={}", orderId);
        distributorOrderService.confirmOrder(orderId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{orderId}/reject")
    public ResponseEntity<Void> rejectOrder(@PathVariable String orderId, 
                                           @Valid @RequestBody RejectOrderRequest request) {
        log.info("❌ 유통사 발주 거절 요청: orderId={}, reason={}", orderId, request.getReason());
        distributorOrderService.rejectOrder(orderId, request.getReason());
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{orderId}/items/{itemId}/price")
    public ResponseEntity<Void> updateItemPrice(@PathVariable String orderId,
                                               @PathVariable Long itemId,
                                               @Valid @RequestBody UpdateItemPriceRequest request) {
        log.info("💰 품목 단가 수정 요청: orderId={}, itemId={}, newPrice={}", orderId, itemId, request.getNewPrice());
        distributorOrderService.updateItemPrice(orderId, itemId, request.getNewPrice());
        return ResponseEntity.ok().build();
    }
}
