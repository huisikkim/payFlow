package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.recruitment.domain.CandidateSkill;
import com.example.payflow.recruitment.domain.ProficiencyLevel;
import lombok.Getter;

@Getter
public class CandidateSkillResponse {
    private final Long id;
    private final String skillName;
    private final ProficiencyLevel proficiencyLevel;
    private final Integer yearsOfExperience;
    private final String description;
    
    public CandidateSkillResponse(CandidateSkill candidateSkill) {
        this.id = candidateSkill.getId();
        this.skillName = candidateSkill.getSkill().getName();
        this.proficiencyLevel = candidateSkill.getProficiencyLevel();
        this.yearsOfExperience = candidateSkill.getYearsOfExperience();
        this.description = candidateSkill.getDescription();
    }
}
