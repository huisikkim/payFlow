package com.example.payflow.ainjob.domain;

public enum JobPostingStatus {
    DRAFT("임시저장"),
    OPEN("공개"),
    CLOSED("마감");
    
    private final String description;
    
    JobPostingStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
