package com.example.payflow.catalog.domain;

public enum DeliveryStatus {
    PREPARING("상품준비중"),
    SHIPPED("배송중"),
    DELIVERED("배송완료");
    
    private final String description;
    
    DeliveryStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
