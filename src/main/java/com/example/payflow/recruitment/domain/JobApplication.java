package com.example.payflow.recruitment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;
    
    // 온톨로지 매칭 스코어 (0-100)
    @Column(nullable = false)
    private double matchingScore;
    
    @Column(length = 1000)
    private String coverLetter;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(nullable = false)
    private LocalDateTime appliedAt;
    
    private LocalDateTime updatedAt;
    
    public static JobApplication create(Candidate candidate, JobPosting jobPosting,
                                       double matchingScore, String coverLetter) {
        JobApplication application = new JobApplication();
        application.candidate = candidate;
        application.jobPosting = jobPosting;
        application.status = ApplicationStatus.APPLIED;
        application.matchingScore = matchingScore;
        application.coverLetter = coverLetter;
        application.appliedAt = LocalDateTime.now();
        return application;
    }
    
    public void updateStatus(ApplicationStatus status, String notes) {
        this.status = status;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateMatchingScore(double score) {
        this.matchingScore = score;
        this.updatedAt = LocalDateTime.now();
    }
}
