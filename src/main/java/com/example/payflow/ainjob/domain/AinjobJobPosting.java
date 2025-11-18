package com.example.payflow.ainjob.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ainjob_job_postings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AinjobJobPosting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(length = 5000)
    private String description;
    
    @Column(nullable = false, length = 50)
    private String position;
    
    @Embedded
    private Qualification qualification;
    
    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobPostingSkill> requiredSkills = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobPostingStatus status;
    
    private LocalDate openDate;
    
    private LocalDate closeDate;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public static AinjobJobPosting create(Long companyId, String title, String description, 
                                          String position, Qualification qualification,
                                          LocalDate openDate, LocalDate closeDate) {
        AinjobJobPosting jobPosting = new AinjobJobPosting();
        jobPosting.companyId = companyId;
        jobPosting.title = title;
        jobPosting.description = description;
        jobPosting.position = position;
        jobPosting.qualification = qualification;
        jobPosting.status = JobPostingStatus.DRAFT;
        jobPosting.openDate = openDate;
        jobPosting.closeDate = closeDate;
        jobPosting.createdAt = LocalDateTime.now();
        return jobPosting;
    }
    
    public void addRequiredSkill(JobPostingSkill skill) {
        this.requiredSkills.add(skill);
        skill.setJobPosting(this);
    }
    
    public void open() {
        if (this.status == JobPostingStatus.OPEN) {
            throw new IllegalStateException("Already opened");
        }
        this.status = JobPostingStatus.OPEN;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void close() {
        if (this.status == JobPostingStatus.CLOSED) {
            throw new IllegalStateException("Already closed");
        }
        this.status = JobPostingStatus.CLOSED;
        this.closeDate = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isApplicable() {
        return status == JobPostingStatus.OPEN 
            && LocalDate.now().isBefore(closeDate);
    }
}
