package com.example.payflow.qualityissue.domain;

public enum RequestAction {
    REFUND("환불"),
    EXCHANGE("교환");
    
    private final String description;
    
    RequestAction(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
