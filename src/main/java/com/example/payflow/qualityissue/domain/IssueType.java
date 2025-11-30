package com.example.payflow.qualityissue.domain;

public enum IssueType {
    POOR_QUALITY("품질 불량"),
    WRONG_ITEM("오배송"),
    DAMAGED("파손"),
    EXPIRED("유통기한 임박/경과"),
    QUANTITY_MISMATCH("수량 불일치");
    
    private final String description;
    
    IssueType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
