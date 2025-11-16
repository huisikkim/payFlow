package com.example.payflow.recruitment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "candidate_skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProficiencyLevel proficiencyLevel;
    
    private Integer yearsOfExperience;
    
    @Column(length = 500)
    private String description;
    
    public static CandidateSkill create(Skill skill, ProficiencyLevel proficiencyLevel,
                                       Integer yearsOfExperience, String description) {
        CandidateSkill candidateSkill = new CandidateSkill();
        candidateSkill.skill = skill;
        candidateSkill.proficiencyLevel = proficiencyLevel;
        candidateSkill.yearsOfExperience = yearsOfExperience;
        candidateSkill.description = description;
        return candidateSkill;
    }
    
    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }
}
