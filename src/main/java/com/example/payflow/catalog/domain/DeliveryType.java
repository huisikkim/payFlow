package com.example.payflow.catalog.domain;

public enum DeliveryType {
    DIRECT("직접배송"),      // 유통업자가 직접 배송
    COURIER("택배배송");     // 택배사를 통한 배송
    
    private final String description;
    
    DeliveryType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
