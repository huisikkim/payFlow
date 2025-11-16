package com.example.payflow.recruitment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "work_experiences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkExperience {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    @Column(nullable = false)
    private String company;
    
    @Column(nullable = false)
    private String position;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @Column(nullable = false)
    private boolean currentlyWorking = false;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 500)
    private String achievements;
    
    public static WorkExperience create(String company, String position,
                                       LocalDate startDate, LocalDate endDate,
                                       boolean currentlyWorking,
                                       String description, String achievements) {
        WorkExperience experience = new WorkExperience();
        experience.company = company;
        experience.position = position;
        experience.startDate = startDate;
        experience.endDate = endDate;
        experience.currentlyWorking = currentlyWorking;
        experience.description = description;
        experience.achievements = achievements;
        return experience;
    }
    
    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }
    
    public int getDurationInMonths() {
        LocalDate end = currentlyWorking ? LocalDate.now() : endDate;
        Period period = Period.between(startDate, end);
        return period.getYears() * 12 + period.getMonths();
    }
}
