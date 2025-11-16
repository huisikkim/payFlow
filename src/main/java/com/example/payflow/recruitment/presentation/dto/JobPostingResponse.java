package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.hr.domain.Position;
import com.example.payflow.recruitment.domain.JobPosting;
import com.example.payflow.recruitment.domain.JobPostingStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class JobPostingResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String departmentName;
    private final Position position;
    private final Integer headcount;
    private final JobPostingStatus status;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<JobRequirementResponse> requirements;
    
    public JobPostingResponse(JobPosting jobPosting) {
        this.id = jobPosting.getId();
        this.title = jobPosting.getTitle();
        this.description = jobPosting.getDescription();
        this.departmentName = jobPosting.getDepartment() != null ? 
            jobPosting.getDepartment().getName() : null;
        this.position = jobPosting.getPosition();
        this.headcount = jobPosting.getHeadcount();
        this.status = jobPosting.getStatus();
        this.startDate = jobPosting.getStartDate();
        this.endDate = jobPosting.getEndDate();
        this.requirements = jobPosting.getRequirements().stream()
            .map(JobRequirementResponse::new)
            .collect(Collectors.toList());
    }
}
