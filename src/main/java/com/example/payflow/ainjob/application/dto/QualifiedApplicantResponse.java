package com.example.payflow.ainjob.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QualifiedApplicantResponse {
    private Long applicantId;
    private String applicantName;
    private String email;
    private String educationLevel;
    private String majorName;
    private List<String> skills;
    private Double totalYearsOfExperience;
    private Integer matchingScore;
    private String matchingReason;
    private Boolean isQualified;
    
    // 매칭 상세 정보
    private Boolean educationMatched;
    private Boolean majorMatched;
    private Boolean skillsMatched;
    private Boolean experienceMatched;
}
