package com.example.payflow.ainjob.application.dto;

import com.example.payflow.ainjob.domain.EducationLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class JobPostingCreateRequest {
    
    private Long companyId;
    private String title;
    private String description;
    private String position;
    private QualificationDto qualification;
    private List<RequiredSkillDto> requiredSkills;
    private LocalDate openDate;
    private LocalDate closeDate;
    
    @Getter
    @NoArgsConstructor
    public static class QualificationDto {
        private EducationLevel minEducationLevel;
        private List<String> acceptedMajors;
        private Integer minYearsOfExperience;
    }
    
    @Getter
    @NoArgsConstructor
    public static class RequiredSkillDto {
        private String skillName;
        private Boolean isRequired;
        private Integer minProficiency;
    }
}
