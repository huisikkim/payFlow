package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.recruitment.domain.JobRequirement;
import com.example.payflow.recruitment.domain.ProficiencyLevel;
import com.example.payflow.recruitment.domain.RequirementType;
import lombok.Getter;

@Getter
public class JobRequirementResponse {
    private final Long id;
    private final String skillName;
    private final RequirementType type;
    private final ProficiencyLevel minProficiency;
    private final Integer minYearsOfExperience;
    private final String description;
    
    public JobRequirementResponse(JobRequirement requirement) {
        this.id = requirement.getId();
        this.skillName = requirement.getSkill().getName();
        this.type = requirement.getType();
        this.minProficiency = requirement.getMinProficiency();
        this.minYearsOfExperience = requirement.getMinYearsOfExperience();
        this.description = requirement.getDescription();
    }
}
