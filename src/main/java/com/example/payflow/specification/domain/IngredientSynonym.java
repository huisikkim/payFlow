package com.example.payflow.specification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 재료 동의어 엔티티
 * 표준 재료명과 동의어 매핑을 저장
 */
@Entity
@Table(name = "ingredient_synonyms", indexes = {
    @Index(name = "idx_synonym", columnList = "synonym"),
    @Index(name = "idx_standard_name", columnList = "standardName")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientSynonym {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String standardName; // 표준 재료명 (예: 양파)
    
    @Column(nullable = false, unique = true)
    private String synonym; // 동의어 (예: 양 파, 황양파)
    
    @Column(nullable = false)
    private Double similarityScore; // 유사도 점수 (0.0 ~ 1.0)
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
