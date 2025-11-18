package com.example.payflow.ainjob.application.dto;

import com.example.payflow.ainjob.domain.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class JobPostingResponse {
    
    private final Long id;
    private final Long companyId;
    private final String title;
    private final String description;
    private final String position;
    private final QualificationDto qualification;
    private final List<RequiredSkillDto> requiredSkills;
    private final JobPostingStatus status;
    private final LocalDate openDate;
    private final LocalDate closeDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public JobPostingResponse(AinjobJobPosting jobPosting) {
        this.id = jobPosting.getId();
        this.companyId = jobPosting.getCompanyId();
        this.title = jobPosting.getTitle();
        this.description = jobPosting.getDescription();
        this.position = jobPosting.getPosition();
        this.qualification = new QualificationDto(jobPosting.getQualification());
        this.requiredSkills = jobPosting.getRequiredSkills().stream()
            .map(RequiredSkillDto::new)
            .collect(Collectors.toList());
        this.status = jobPosting.getStatus();
        this.openDate = jobPosting.getOpenDate();
        this.closeDate = jobPosting.getCloseDate();
        this.createdAt = jobPosting.getCreatedAt();
        this.updatedAt = jobPosting.getUpdatedAt();
    }
    
    @Getter
    public static class QualificationDto {
        private final EducationLevel minEducationLevel;
        private final List<String> acceptedMajors;
        private final Integer minYearsOfExperience;
        
        public QualificationDto(Qualification qualification) {
            this.minEducationLevel = qualification.getMinEducationLevel();
            this.acceptedMajors = parseAcceptedMajors(qualification.getAcceptedMajors());
            this.minYearsOfExperience = qualification.getMinYearsOfExperience();
        }
        
        private List<String> parseAcceptedMajors(String json) {
            if (json == null || json.isEmpty()) {
                return List.of();
            }
            try {
                Gson gson = new Gson();
                return gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
            } catch (Exception e) {
                return List.of();
            }
        }
    }
    
    @Getter
    public static class RequiredSkillDto {
        private final Long id;
        private final String skillName;
        private final Boolean isRequired;
        private final Integer minProficiency;
        
        public RequiredSkillDto(JobPostingSkill skill) {
            this.id = skill.getId();
            this.skillName = skill.getSkill().getName();
            this.isRequired = skill.getIsRequired();
            this.minProficiency = skill.getMinProficiency();
        }
    }
}
