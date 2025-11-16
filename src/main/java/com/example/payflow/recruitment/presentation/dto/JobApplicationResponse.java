package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.recruitment.domain.ApplicationStatus;
import com.example.payflow.recruitment.domain.JobApplication;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JobApplicationResponse {
    private final Long id;
    private final Long candidateId;
    private final String candidateName;
    private final Long jobPostingId;
    private final String jobTitle;
    private final ApplicationStatus status;
    private final double matchingScore;
    private final String coverLetter;
    private final LocalDateTime appliedAt;
    
    public JobApplicationResponse(JobApplication application) {
        this.id = application.getId();
        this.candidateId = application.getCandidate().getId();
        this.candidateName = application.getCandidate().getName();
        this.jobPostingId = application.getJobPosting().getId();
        this.jobTitle = application.getJobPosting().getTitle();
        this.status = application.getStatus();
        this.matchingScore = application.getMatchingScore();
        this.coverLetter = application.getCoverLetter();
        this.appliedAt = application.getAppliedAt();
    }
}
