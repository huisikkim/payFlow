package com.example.payflow.catalog.presentation;

import com.example.payflow.catalog.application.OrderCartService;
import com.example.payflow.catalog.domain.OrderCart;
import com.example.payflow.catalog.presentation.dto.AddToCartRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class OrderCartController {
    
    private final OrderCartService cartService;
    
    /**
     * 장바구니에 상품 추가
     * POST /api/cart/add
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<OrderCart> addToCart(@Valid @RequestBody AddToCartRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.info("=== 장바구니 추가 요청 ===");
        log.info("Request: {}", request);
        log.info("Authentication: {}", authentication);
        log.info("Principal: {}", authentication != null ? authentication.getPrincipal() : "null");
        log.info("Authorities: {}", authentication != null ? authentication.getAuthorities() : "null");
        
        String storeId = authentication.getName();
        log.info("storeId: {}", storeId);
        
        OrderCart cart = cartService.addToCart(storeId, request);
        log.info("장바구니 추가 성공: productId={}, quantity={}", request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(cart);
    }
    
    /**
     * 장바구니 조회
     * GET /api/cart/{distributorId}
     */
    @GetMapping("/{distributorId}")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<OrderCart> getCart(@PathVariable String distributorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.info("=== 장바구니 조회 요청 ===");
        log.info("distributorId: {}", distributorId);
        log.info("Authentication: {}", authentication);
        log.info("Principal: {}", authentication != null ? authentication.getPrincipal() : "null");
        log.info("Authorities: {}", authentication != null ? authentication.getAuthorities() : "null");
        log.info("IsAuthenticated: {}", authentication != null ? authentication.isAuthenticated() : "false");
        
        String storeId = authentication.getName();
        log.info("storeId: {}", storeId);
        
        OrderCart cart = cartService.getCart(storeId, distributorId);
        log.info("장바구니 조회 성공: {} items", cart.getItems().size());
        return ResponseEntity.ok(cart);
    }
    
    /**
     * 장바구니 아이템 수량 변경
     * PUT /api/cart/{distributorId}/items/{itemId}
     */
    @PutMapping("/{distributorId}/items/{itemId}")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<OrderCart> updateItemQuantity(
            @PathVariable String distributorId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        OrderCart cart = cartService.updateItemQuantity(storeId, distributorId, itemId, quantity);
        return ResponseEntity.ok(cart);
    }
    
    /**
     * 장바구니 아이템 삭제
     * DELETE /api/cart/{distributorId}/items/{itemId}
     */
    @DeleteMapping("/{distributorId}/items/{itemId}")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<OrderCart> removeItem(
            @PathVariable String distributorId,
            @PathVariable Long itemId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        OrderCart cart = cartService.removeItem(storeId, distributorId, itemId);
        return ResponseEntity.ok(cart);
    }
    
    /**
     * 장바구니 비우기
     * DELETE /api/cart/{distributorId}
     */
    @DeleteMapping("/{distributorId}")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<String> clearCart(@PathVariable String distributorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        cartService.clearCart(storeId, distributorId);
        return ResponseEntity.ok("장바구니가 비워졌습니다.");
    }
}
