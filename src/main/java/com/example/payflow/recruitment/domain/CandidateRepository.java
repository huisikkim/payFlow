package com.example.payflow.recruitment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    Optional<Candidate> findByEmail(String email);
    
    @Query("SELECT DISTINCT c FROM Candidate c " +
           "JOIN c.skills cs " +
           "WHERE cs.skill.id = :skillId")
    List<Candidate> findBySkill(@Param("skillId") Long skillId);
    
    @Query("SELECT c FROM Candidate c WHERE c.education >= :minEducation")
    List<Candidate> findByMinEducation(@Param("minEducation") EducationLevel minEducation);
}
