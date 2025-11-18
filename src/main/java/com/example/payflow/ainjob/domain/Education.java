package com.example.payflow.ainjob.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ainjob_educations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Education {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EducationLevel level;
    
    @Column(nullable = false, length = 200)
    private String schoolName;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @Column(length = 20)
    private String status; // GRADUATED, IN_PROGRESS, LEAVE
    
    public static Education create(EducationLevel level, Major major, String schoolName, 
                                   LocalDate startDate, LocalDate endDate, String status) {
        Education education = new Education();
        education.level = level;
        education.major = major;
        education.schoolName = schoolName;
        education.startDate = startDate;
        education.endDate = endDate;
        education.status = status;
        return education;
    }
    
    void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }
}
