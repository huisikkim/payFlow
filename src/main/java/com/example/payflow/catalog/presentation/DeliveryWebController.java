package com.example.payflow.catalog.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/deliveries")
public class DeliveryWebController {
    
    /**
     * 유통업자 배송 관리 페이지
     */
    @GetMapping("/distributor")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public String distributorDeliveryPage() {
        return "delivery/distributor";
    }
    
    /**
     * 매장 배송 조회 페이지
     */
    @GetMapping("/store")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public String storeDeliveryPage() {
        return "delivery/store";
    }
    
    /**
     * 배송 상세 페이지
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public String deliveryDetailPage(@PathVariable Long orderId) {
        return "delivery/detail";
    }
}
