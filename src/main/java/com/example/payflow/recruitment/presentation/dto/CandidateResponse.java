package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.recruitment.domain.Candidate;
import com.example.payflow.recruitment.domain.EducationLevel;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CandidateResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final String phone;
    private final EducationLevel education;
    private final String university;
    private final String major;
    private final String summary;
    private final int totalYearsOfExperience;
    private final List<CandidateSkillResponse> skills;
    private final List<WorkExperienceResponse> experiences;
    
    public CandidateResponse(Candidate candidate) {
        this.id = candidate.getId();
        this.name = candidate.getName();
        this.email = candidate.getEmail();
        this.phone = candidate.getPhone();
        this.education = candidate.getEducation();
        this.university = candidate.getUniversity();
        this.major = candidate.getMajor();
        this.summary = candidate.getSummary();
        this.totalYearsOfExperience = candidate.getTotalYearsOfExperience();
        this.skills = candidate.getSkills().stream()
            .map(CandidateSkillResponse::new)
            .collect(Collectors.toList());
        this.experiences = candidate.getExperiences().stream()
            .map(WorkExperienceResponse::new)
            .collect(Collectors.toList());
    }
}
