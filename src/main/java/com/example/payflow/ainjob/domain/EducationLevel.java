package com.example.payflow.ainjob.domain;

public enum EducationLevel {
    HIGH_SCHOOL("고등학교"),
    ASSOCIATE("전문학사"),
    BACHELOR("학사"),
    MASTER("석사"),
    DOCTORATE("박사");
    
    private final String description;
    
    EducationLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
