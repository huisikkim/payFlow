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
@Table(name = "ainjob_applicants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Applicant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    private LocalDate birthDate;
    
    @Embedded
    private Address address;
    
    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new ArrayList<>();
    
    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Career> careers = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public static Applicant create(String name, String email, String phone, LocalDate birthDate, Address address) {
        Applicant applicant = new Applicant();
        applicant.name = name;
        applicant.email = email;
        applicant.phone = phone;
        applicant.birthDate = birthDate;
        applicant.address = address;
        applicant.createdAt = LocalDateTime.now();
        return applicant;
    }
    
    public void addEducation(Education education) {
        this.educations.add(education);
        education.setApplicant(this);
    }
    
    public void addCareer(Career career) {
        this.careers.add(career);
        career.setApplicant(this);
    }
    
    public void update(String name, String phone, LocalDate birthDate, Address address) {
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getTotalYearsOfExperience() {
        return careers.stream()
            .mapToInt(Career::getYearsOfExperience)
            .sum();
    }
}
