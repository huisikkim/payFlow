package com.example.payflow.ingredientorder.presentation;

import com.example.payflow.ingredientorder.application.IngredientOrderService;
import com.example.payflow.ingredientorder.presentation.dto.CreateIngredientOrderRequest;
import com.example.payflow.ingredientorder.presentation.dto.IngredientOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient-orders")
@RequiredArgsConstructor
@Slf4j
public class IngredientOrderController {
    
    private final IngredientOrderService orderService;
    
    @PostMapping
    public ResponseEntity<IngredientOrderResponse> createOrder(@Valid @RequestBody CreateIngredientOrderRequest request) {
        log.info("📦 식자재 발주 요청: storeId={}, distributorId={}", request.getStoreId(), request.getDistributorId());
        IngredientOrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<IngredientOrderResponse> getOrder(@PathVariable String orderId) {
        IngredientOrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<IngredientOrderResponse>> getOrdersByStore(@PathVariable String storeId) {
        List<IngredientOrderResponse> orders = orderService.getOrdersByStore(storeId);
        return ResponseEntity.ok(orders);
    }
}
