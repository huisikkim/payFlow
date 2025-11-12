package com.example.payflow.chatbot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String industry;

    @Column(nullable = false)
    private Long minSalary;

    @Column(nullable = false)
    private Long maxSalary;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String experienceLevel; // ENTRY, JUNIOR, SENIOR, EXECUTIVE

    @Column(nullable = false)
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERN

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    public Job(String companyName, String position, String region, String industry,
               Long minSalary, Long maxSalary, String description,
               String experienceLevel, String employmentType,
               LocalDateTime expiresAt) {
        this.companyName = companyName;
        this.position = position;
        this.region = region;
        this.industry = industry;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.description = description;
        this.experienceLevel = experienceLevel;
        this.employmentType = employmentType;
        this.postedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public String getSalaryRange() {
        return String.format("%,d만원 ~ %,d만원", minSalary / 10000, maxSalary / 10000);
    }
}
