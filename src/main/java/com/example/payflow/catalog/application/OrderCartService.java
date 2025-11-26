package com.example.payflow.catalog.application;

import com.example.payflow.catalog.domain.*;
import com.example.payflow.catalog.presentation.dto.AddToCartRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCartService {
    
    private final OrderCartRepository cartRepository;
    private final ProductCatalogRepository catalogRepository;
    
    /**
     * 장바구니에 상품 추가
     */
    @Transactional
    public OrderCart addToCart(String storeId, AddToCartRequest request) {
        // 상품 조회
        ProductCatalog product = catalogRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + request.getProductId()));
        
        // 판매 가능 여부 확인
        if (!product.canOrder(request.getQuantity())) {
            throw new IllegalStateException("주문할 수 없는 상품입니다");
        }
        
        // 장바구니 조회 또는 생성
        OrderCart cart = cartRepository.findByStoreIdAndDistributorId(storeId, product.getDistributorId())
                .orElseGet(() -> {
                    OrderCart newCart = OrderCart.builder()
                            .storeId(storeId)
                            .distributorId(product.getDistributorId())
                            .build();
                    return cartRepository.save(newCart);
                });
        
        // 이미 장바구니에 있는 상품인지 확인
        OrderCartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            // 수량 업데이트
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            // 새 아이템 추가
            OrderCartItem newItem = OrderCartItem.builder()
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .unitPrice(product.getUnitPrice())
                    .unit(product.getUnit())
                    .quantity(request.getQuantity())
                    .imageUrl(product.getImageUrl())
                    .build();
            cart.addItem(newItem);
        }
        
        cart.calculateTotal();
        OrderCart saved = cartRepository.save(cart);
        
        log.info("✅ 장바구니 추가: storeId={}, productId={}, quantity={}", 
                storeId, request.getProductId(), request.getQuantity());
        
        return saved;
    }
    
    /**
     * 장바구니 조회
     */
    @Transactional(readOnly = true)
    public OrderCart getCart(String storeId, String distributorId) {
        return cartRepository.findByStoreIdAndDistributorId(storeId, distributorId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 비어있습니다"));
    }
    
    /**
     * 장바구니 아이템 수량 변경
     */
    @Transactional
    public OrderCart updateItemQuantity(String storeId, String distributorId, Long itemId, Integer quantity) {
        OrderCart cart = cartRepository.findByStoreIdAndDistributorId(storeId, distributorId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다"));
        
        OrderCartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다"));
        
        // 상품 재고 확인
        ProductCatalog product = catalogRepository.findById(item.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다"));
        
        if (!product.canOrder(quantity)) {
            throw new IllegalStateException("주문할 수 없는 수량입니다");
        }
        
        item.setQuantity(quantity);
        cart.calculateTotal();
        
        OrderCart saved = cartRepository.save(cart);
        log.info("✅ 장바구니 수량 변경: itemId={}, quantity={}", itemId, quantity);
        
        return saved;
    }
    
    /**
     * 장바구니 아이템 삭제
     */
    @Transactional
    public OrderCart removeItem(String storeId, String distributorId, Long itemId) {
        OrderCart cart = cartRepository.findByStoreIdAndDistributorId(storeId, distributorId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다"));
        
        OrderCartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다"));
        
        cart.removeItem(item);
        
        OrderCart saved = cartRepository.save(cart);
        log.info("✅ 장바구니 아이템 삭제: itemId={}", itemId);
        
        return saved;
    }
    
    /**
     * 장바구니 비우기
     */
    @Transactional
    public void clearCart(String storeId, String distributorId) {
        cartRepository.deleteByStoreIdAndDistributorId(storeId, distributorId);
        log.info("✅ 장바구니 비우기: storeId={}, distributorId={}", storeId, distributorId);
    }
}
