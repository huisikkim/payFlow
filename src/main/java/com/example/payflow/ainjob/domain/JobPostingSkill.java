package com.example.payflow.ainjob.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ainjob_job_posting_skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPostingSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private AinjobJobPosting jobPosting;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private AinjobSkill skill;
    
    @Column(nullable = false)
    private Boolean isRequired;
    
    private Integer minProficiency;
    
    public static JobPostingSkill create(AinjobSkill skill, Boolean isRequired, Integer minProficiency) {
        JobPostingSkill jobPostingSkill = new JobPostingSkill();
        jobPostingSkill.skill = skill;
        jobPostingSkill.isRequired = isRequired;
        jobPostingSkill.minProficiency = minProficiency;
        return jobPostingSkill;
    }
    
    void setJobPosting(AinjobJobPosting jobPosting) {
        this.jobPosting = jobPosting;
    }
}
