package com.example.payflow.recruitment.domain;

public enum SkillCategory {
    PROGRAMMING_LANGUAGE("프로그래밍 언어"),
    FRAMEWORK("프레임워크"),
    DATABASE("데이터베이스"),
    CLOUD("클라우드"),
    DEVOPS("데브옵스"),
    SOFT_SKILL("소프트 스킬"),
    TOOL("도구"),
    METHODOLOGY("방법론");
    
    private final String displayName;
    
    SkillCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
