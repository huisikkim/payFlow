package com.example.payflow.recruitment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_requirements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobRequirement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequirementType type;
    
    @Enumerated(EnumType.STRING)
    private ProficiencyLevel minProficiency;
    
    private Integer minYearsOfExperience;
    
    @Column(length = 500)
    private String description;
    
    public static JobRequirement create(Skill skill, RequirementType type,
                                       ProficiencyLevel minProficiency,
                                       Integer minYearsOfExperience,
                                       String description) {
        JobRequirement requirement = new JobRequirement();
        requirement.skill = skill;
        requirement.type = type;
        requirement.minProficiency = minProficiency;
        requirement.minYearsOfExperience = minYearsOfExperience;
        requirement.description = description;
        return requirement;
    }
    
    public void setJobPosting(JobPosting jobPosting) {
        this.jobPosting = jobPosting;
    }
}
