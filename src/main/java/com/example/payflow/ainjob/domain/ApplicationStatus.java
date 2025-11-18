package com.example.payflow.ainjob.domain;

public enum ApplicationStatus {
    APPLIED("지원"),
    DOCUMENT_PASS("서류합격"),
    INTERVIEW_1("1차면접"),
    INTERVIEW_2("2차면접"),
    FINAL_PASS("최종합격"),
    REJECTED("불합격");
    
    private final String description;
    
    ApplicationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
