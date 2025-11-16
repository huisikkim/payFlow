package com.example.payflow.recruitment.domain;

public enum ApplicationStatus {
    APPLIED("지원 완료"),
    SCREENING("서류 심사 중"),
    SCREENING_PASSED("서류 합격"),
    SCREENING_FAILED("서류 불합격"),
    INTERVIEW_SCHEDULED("면접 예정"),
    INTERVIEW_COMPLETED("면접 완료"),
    OFFER("최종 합격"),
    REJECTED("불합격"),
    WITHDRAWN("지원 취소");
    
    private final String displayName;
    
    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
