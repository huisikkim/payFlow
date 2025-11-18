package com.example.payflow.ainjob.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Qualification {
    
    @Enumerated(EnumType.STRING)
    private EducationLevel minEducationLevel;
    
    private String acceptedMajors; // JSON 형태로 저장
    
    private Integer minYearsOfExperience;
    
    public Qualification(EducationLevel minEducationLevel, String acceptedMajors, Integer minYearsOfExperience) {
        this.minEducationLevel = minEducationLevel;
        this.acceptedMajors = acceptedMajors;
        this.minYearsOfExperience = minYearsOfExperience;
    }
    
    public boolean meetsEducationRequirement(EducationLevel level) {
        return level.ordinal() >= minEducationLevel.ordinal();
    }
}
