package com.example.payflow.distributor.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "distributors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Distributor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String distributorId;
    
    @Column(nullable = false)
    private String distributorName;
    
    private String businessNumber;
    private String phoneNumber;
    private String managerName;
    private String email;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Distributor(String distributorId, String distributorName, String businessNumber, 
                      String phoneNumber, String managerName) {
        this.distributorId = distributorId;
        this.distributorName = distributorName;
        this.businessNumber = businessNumber;
        this.phoneNumber = phoneNumber;
        this.managerName = managerName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
