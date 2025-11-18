package com.example.payflow.ainjob.application.dto;

import com.example.payflow.ainjob.domain.ApplicationStatus;
import com.example.payflow.ainjob.domain.ApplicationStatusHistory;
import com.example.payflow.ainjob.domain.ApplicationTracking;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ApplicationResponse {
    
    private final Long id;
    private final Long applicantId;
    private final Long jobPostingId;
    private final Long resumeId;
    private final ApplicationStatus status;
    private final List<StatusHistoryDto> statusHistories;
    private final LocalDateTime appliedAt;
    private final LocalDateTime updatedAt;
    
    public ApplicationResponse(ApplicationTracking tracking) {
        this.id = tracking.getId();
        this.applicantId = tracking.getApplicantId();
        this.jobPostingId = tracking.getJobPostingId();
        this.resumeId = tracking.getResumeId();
        this.status = tracking.getStatus();
        this.statusHistories = tracking.getStatusHistories().stream()
            .map(StatusHistoryDto::new)
            .collect(Collectors.toList());
        this.appliedAt = tracking.getAppliedAt();
        this.updatedAt = tracking.getUpdatedAt();
    }
    
    @Getter
    public static class StatusHistoryDto {
        private final Long id;
        private final ApplicationStatus fromStatus;
        private final ApplicationStatus toStatus;
        private final String reason;
        private final String changedBy;
        private final LocalDateTime changedAt;
        
        public StatusHistoryDto(ApplicationStatusHistory history) {
            this.id = history.getId();
            this.fromStatus = history.getFromStatus();
            this.toStatus = history.getToStatus();
            this.reason = history.getReason();
            this.changedBy = history.getChangedBy();
            this.changedAt = history.getChangedAt();
        }
    }
}
