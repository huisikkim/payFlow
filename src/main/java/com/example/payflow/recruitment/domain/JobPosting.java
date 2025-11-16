package com.example.payflow.recruitment.domain;

import com.example.payflow.hr.domain.Department;
import com.example.payflow.hr.domain.Position;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_postings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000, nullable = false)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;
    
    @Column(nullable = false)
    private Integer headcount = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobPostingStatus status;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    // 온톨로지: 요구사항
    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobRequirement> requirements = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public static JobPosting create(String title, String description, Department department,
                                   Position position, Integer headcount,
                                   LocalDate startDate, LocalDate endDate) {
        JobPosting posting = new JobPosting();
        posting.title = title;
        posting.description = description;
        posting.department = department;
        posting.position = position;
        posting.headcount = headcount;
        posting.status = JobPostingStatus.DRAFT;
        posting.startDate = startDate;
        posting.endDate = endDate;
        posting.createdAt = LocalDateTime.now();
        return posting;
    }
    
    public void addRequirement(JobRequirement requirement) {
        this.requirements.add(requirement);
        requirement.setJobPosting(this);
    }
    
    public void publish() {
        this.status = JobPostingStatus.OPEN;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void close() {
        this.status = JobPostingStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void update(String title, String description, Integer headcount,
                      LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.headcount = headcount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }
}
