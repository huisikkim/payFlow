package com.example.payflow.recruitment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    Optional<Skill> findByName(String name);
    
    List<Skill> findByCategory(SkillCategory category);
    
    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Skill> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT s FROM Skill s JOIN s.similarSkills ss WHERE ss.id = :skillId")
    List<Skill> findSimilarSkills(@Param("skillId") Long skillId);
}
