package com.example.payflow.store.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String storeId;
    
    @Column(nullable = false)
    private String storeName;
    
    @Column(nullable = false)
    private String ownerName;
    
    private String phoneNumber;
    private String address;
    private String businessNumber;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Store(String storeId, String storeName, String ownerName, String phoneNumber, String address) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.ownerName = ownerName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
