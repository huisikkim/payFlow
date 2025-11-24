package com.example.payflow.specification.domain;

public enum ProcessingStatus {
    UPLOADED("업로드됨"),
    TEXT_EXTRACTED("텍스트 추출됨"),
    PARSING("파싱 중"),
    PARSED("파싱 완료"),
    ERROR("오류 발생");
    
    private final String description;
    
    ProcessingStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
