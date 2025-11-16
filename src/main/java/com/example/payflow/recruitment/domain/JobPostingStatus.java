package com.example.payflow.recruitment.domain;

public enum JobPostingStatus {
    DRAFT("임시저장"),
    OPEN("공개"),
    CLOSED("마감"),
    CANCELLED("취소");
    
    private final String displayName;
    
    JobPostingStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
