package com.example.payflow.recruitment.domain;

public enum EducationLevel {
    HIGH_SCHOOL("고등학교 졸업", 1),
    ASSOCIATE("전문학사", 2),
    BACHELOR("학사", 3),
    MASTER("석사", 4),
    DOCTORATE("박사", 5);
    
    private final String displayName;
    private final int level;
    
    EducationLevel(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
}
