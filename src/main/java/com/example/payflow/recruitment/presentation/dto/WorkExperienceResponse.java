package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.recruitment.domain.WorkExperience;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class WorkExperienceResponse {
    private final Long id;
    private final String company;
    private final String position;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final boolean currentlyWorking;
    private final String description;
    private final int durationInMonths;
    
    public WorkExperienceResponse(WorkExperience experience) {
        this.id = experience.getId();
        this.company = experience.getCompany();
        this.position = experience.getPosition();
        this.startDate = experience.getStartDate();
        this.endDate = experience.getEndDate();
        this.currentlyWorking = experience.isCurrentlyWorking();
        this.description = experience.getDescription();
        this.durationInMonths = experience.getDurationInMonths();
    }
}
