package com.example.payflow.hr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String employeeNumber;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;
    
    @Column(nullable = false)
    private LocalDate joinDate;
    
    private LocalDate resignDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentStatus status;
    
    @Column(length = 500)
    private String address;
    
    private LocalDate birthDate;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(length = 500)
    private String profileImageUrl;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public static Employee create(String employeeNumber, String name, String email, 
                                  String phone, Department department, Position position, 
                                  LocalDate joinDate) {
        Employee employee = new Employee();
        employee.employeeNumber = employeeNumber;
        employee.name = name;
        employee.email = email;
        employee.phone = phone;
        employee.department = department;
        employee.position = position;
        employee.joinDate = joinDate;
        employee.status = EmploymentStatus.ACTIVE;
        employee.createdAt = LocalDateTime.now();
        return employee;
    }
    
    public void update(String name, String email, String phone, String address, 
                      LocalDate birthDate, String notes) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.birthDate = birthDate;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void changeDepartment(Department department) {
        this.department = department;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void promote(Position position) {
        this.position = position;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void resign(LocalDate resignDate) {
        this.resignDate = resignDate;
        this.status = EmploymentStatus.RESIGNED;
        this.updatedAt = LocalDateTime.now();
    }
}
