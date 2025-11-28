package com.example.payflow.catalog.domain;

public enum ReviewType {
    STORE_TO_DISTRIBUTOR("가게사장님 → 유통업자"),
    DISTRIBUTOR_TO_STORE("유통업자 → 가게사장님");
    
    private final String description;
    
    ReviewType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
