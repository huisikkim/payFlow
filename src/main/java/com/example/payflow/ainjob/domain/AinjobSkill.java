package com.example.payflow.ainjob.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ainjob_skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AinjobSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 50)
    private String category;
    
    @Column(length = 500)
    private String description;
    
    public static AinjobSkill create(String name, String category, String description) {
        AinjobSkill skill = new AinjobSkill();
        skill.name = name;
        skill.category = category;
        skill.description = description;
        return skill;
    }
}
