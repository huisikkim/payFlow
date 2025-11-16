package com.example.payflow.hr.domain;

public enum Position {
    CEO("대표이사"),
    CTO("기술이사"),
    CFO("재무이사"),
    DIRECTOR("이사"),
    GENERAL_MANAGER("부장"),
    MANAGER("차장"),
    ASSISTANT_MANAGER("과장"),
    SENIOR("대리"),
    STAFF("사원"),
    INTERN("인턴");
    
    private final String displayName;
    
    Position(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
