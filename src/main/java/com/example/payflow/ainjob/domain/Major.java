package com.example.payflow.ainjob.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ainjob_majors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Major {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 50)
    private String category;
    
    public static Major create(String name, String category) {
        Major major = new Major();
        major.name = name;
        major.category = category;
        return major;
    }
}
