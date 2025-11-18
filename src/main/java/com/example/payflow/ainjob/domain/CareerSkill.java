package com.example.payflow.ainjob.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ainjob_career_skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareerSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private AinjobSkill skill;
    
    @Column(nullable = false)
    private Integer proficiencyLevel; // 1-5
    
    public static CareerSkill create(AinjobSkill skill, Integer proficiencyLevel) {
        CareerSkill careerSkill = new CareerSkill();
        careerSkill.skill = skill;
        careerSkill.proficiencyLevel = proficiencyLevel;
        return careerSkill;
    }
    
    void setCareer(Career career) {
        this.career = career;
    }
}
