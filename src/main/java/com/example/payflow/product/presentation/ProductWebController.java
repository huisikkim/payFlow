package com.example.payflow.product.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pickswap")
@RequiredArgsConstructor
public class ProductWebController {
    
    /**
     * Pick Swap 홈 (상품 피드)
     */
    @GetMapping
    public String home() {
        return "pickswap/home";
    }
    
    /**
     * 상품 상세 페이지
     */
    @GetMapping("/products/{productId}")
    public String productDetail(@PathVariable Long productId) {
        return "pickswap/product-detail";
    }
    
    /**
     * 상품 등록 페이지
     */
    @GetMapping("/sell")
    public String sellProduct() {
        return "pickswap/sell";
    }
    
    /**
     * 내 상품 관리 페이지
     */
    @GetMapping("/my-products")
    public String myProducts() {
        return "pickswap/my-products";
    }
}
