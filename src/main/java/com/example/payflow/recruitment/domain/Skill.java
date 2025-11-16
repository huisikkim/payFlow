package com.example.payflow.recruitment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Skill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category;
    
    @Column(length = 500)
    private String description;
    
    // 온톨로지: 유사 기술 관계
    @ManyToMany
    @JoinTable(
        name = "skill_similarities",
        joinColumns = @JoinColumn(name = "skill_id"),
        inverseJoinColumns = @JoinColumn(name = "similar_skill_id")
    )
    private Set<Skill> similarSkills = new HashSet<>();
    
    // 유사도 점수 (0.0 ~ 1.0)
    @Column(nullable = false)
    private double defaultSimilarityScore = 0.8;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public static Skill create(String name, SkillCategory category, String description) {
        Skill skill = new Skill();
        skill.name = name;
        skill.category = category;
        skill.description = description;
        skill.createdAt = LocalDateTime.now();
        return skill;
    }
    
    public void addSimilarSkill(Skill similarSkill) {
        this.similarSkills.add(similarSkill);
        similarSkill.getSimilarSkills().add(this);
    }
    
    public void setSimilarityScore(double score) {
        this.defaultSimilarityScore = Math.max(0.0, Math.min(1.0, score));
    }
}
