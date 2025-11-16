package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.recruitment.application.CandidateMatchingService;
import lombok.Getter;

@Getter
public class MatchingDetailResponse {
    private final double totalScore;
    private final double requiredSkillScore;
    private final double preferredSkillScore;
    private final double experienceScore;
    private final double educationScore;
    
    public MatchingDetailResponse(CandidateMatchingService.MatchingDetail detail) {
        this.totalScore = detail.getTotalScore();
        this.requiredSkillScore = detail.getRequiredSkillScore();
        this.preferredSkillScore = detail.getPreferredSkillScore();
        this.experienceScore = detail.getExperienceScore();
        this.educationScore = detail.getEducationScore();
    }
}
