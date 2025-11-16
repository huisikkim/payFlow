package com.example.payflow.recruitment.domain;

public enum RequirementType {
    REQUIRED("필수"),
    PREFERRED("우대");
    
    private final String displayName;
    
    RequirementType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
