package com.example.payflow.ainjob.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantMatchingScore {
    private Long applicantId;
    private Long jobPostingId;
    private Integer totalScore;
    private Integer maxScore;
    private Double matchingPercentage;
    
    // 세부 점수
    private Integer educationScore;
    private Integer majorScore;
    private Integer skillScore;
    private Integer experienceScore;
    
    // 매칭 상세
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String recommendation;
}
