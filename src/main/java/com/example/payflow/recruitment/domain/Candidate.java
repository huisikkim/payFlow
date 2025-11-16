package com.example.payflow.recruitment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Candidate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    private LocalDate birthDate;
    
    @Column(length = 500)
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EducationLevel education;
    
    @Column(length = 200)
    private String university;
    
    @Column(length = 200)
    private String major;
    
    @Column(length = 2000)
    private String summary;
    
    @Column(length = 500)
    private String resumeUrl;
    
    // 온톨로지: 보유 기술
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CandidateSkill> skills = new ArrayList<>();
    
    // 온톨로지: 경력
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkExperience> experiences = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public static Candidate create(String name, String email, String phone,
                                  EducationLevel education, String university, String major) {
        Candidate candidate = new Candidate();
        candidate.name = name;
        candidate.email = email;
        candidate.phone = phone;
        candidate.education = education;
        candidate.university = university;
        candidate.major = major;
        candidate.createdAt = LocalDateTime.now();
        return candidate;
    }
    
    public void addSkill(CandidateSkill skill) {
        this.skills.add(skill);
        skill.setCandidate(this);
    }
    
    public void addExperience(WorkExperience experience) {
        this.experiences.add(experience);
        experience.setCandidate(this);
    }
    
    public void update(String name, String phone, String address, LocalDate birthDate,
                      String summary, String resumeUrl) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.birthDate = birthDate;
        this.summary = summary;
        this.resumeUrl = resumeUrl;
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getTotalYearsOfExperience() {
        return experiences.stream()
            .mapToInt(WorkExperience::getDurationInMonths)
            .sum() / 12;
    }
}
