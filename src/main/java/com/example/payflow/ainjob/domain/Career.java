package com.example.payflow.ainjob.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ainjob_careers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Career {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;
    
    @Column(nullable = false, length = 200)
    private String companyName;
    
    @Column(length = 100)
    private String position;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @OneToMany(mappedBy = "career", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CareerSkill> skills = new ArrayList<>();
    
    public static Career create(String companyName, String position, String description,
                               LocalDate startDate, LocalDate endDate) {
        Career career = new Career();
        career.companyName = companyName;
        career.position = position;
        career.description = description;
        career.startDate = startDate;
        career.endDate = endDate;
        return career;
    }
    
    void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }
    
    public void addSkill(CareerSkill skill) {
        this.skills.add(skill);
        skill.setCareer(this);
    }
    
    public int getYearsOfExperience() {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return Period.between(startDate, end).getYears();
    }
}
