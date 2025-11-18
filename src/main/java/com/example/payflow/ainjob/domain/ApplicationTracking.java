package com.example.payflow.ainjob.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ainjob_application_trackings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"applicant_id", "job_posting_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;
    
    @Column(name = "job_posting_id", nullable = false)
    private Long jobPostingId;
    
    @Column(name = "resume_id")
    private Long resumeId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;
    
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt DESC")
    private List<ApplicationStatusHistory> statusHistories = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime appliedAt;
    
    private LocalDateTime updatedAt;
    
    public static ApplicationTracking create(Long applicantId, Long jobPostingId, Long resumeId) {
        ApplicationTracking tracking = new ApplicationTracking();
        tracking.applicantId = applicantId;
        tracking.jobPostingId = jobPostingId;
        tracking.resumeId = resumeId;
        tracking.status = ApplicationStatus.APPLIED;
        tracking.appliedAt = LocalDateTime.now();
        
        // 초기 이력 추가
        ApplicationStatusHistory initialHistory = ApplicationStatusHistory.create(
            null, ApplicationStatus.APPLIED, "지원 접수", "SYSTEM"
        );
        tracking.addStatusHistory(initialHistory);
        
        return tracking;
    }
    
    private void addStatusHistory(ApplicationStatusHistory history) {
        this.statusHistories.add(history);
        history.setApplication(this);
    }
    
    public void changeStatus(ApplicationStatus newStatus, String reason, String changedBy) {
        validateStatusTransition(this.status, newStatus);
        
        ApplicationStatusHistory history = ApplicationStatusHistory.create(
            this.status, newStatus, reason, changedBy
        );
        addStatusHistory(history);
        
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
    
    private void validateStatusTransition(ApplicationStatus from, ApplicationStatus to) {
        if (to == ApplicationStatus.REJECTED) {
            return; // REJECTED는 어느 단계에서나 가능
        }
        
        if (to.ordinal() < from.ordinal()) {
            throw new IllegalStateException(
                "Cannot move backward from " + from + " to " + to
            );
        }
    }
}
