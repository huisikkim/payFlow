package com.example.payflow.qualityissue.domain;

public enum IssueStatus {
    SUBMITTED("접수됨"),
    REVIEWING("검토 중"),
    APPROVED("승인됨"),
    REJECTED("거절됨"),
    PICKUP_SCHEDULED("수거 예정"),
    PICKED_UP("수거 완료"),
    REFUNDED("환불 완료"),
    EXCHANGED("교환 완료");
    
    private final String description;
    
    IssueStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
