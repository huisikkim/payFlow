package com.example.payflow.ainjob.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AinjobSkillRepository extends JpaRepository<AinjobSkill, Long> {
    
    Optional<AinjobSkill> findByName(String name);
}
