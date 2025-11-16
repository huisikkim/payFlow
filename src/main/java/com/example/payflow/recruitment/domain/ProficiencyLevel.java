package com.example.payflow.recruitment.domain;

public enum ProficiencyLevel {
    BEGINNER("초급", 1),
    INTERMEDIATE("중급", 2),
    ADVANCED("고급", 3),
    EXPERT("전문가", 4);
    
    private final String displayName;
    private final int level;
    
    ProficiencyLevel(String displayName, int level) {
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
