package com.example.payflow.specification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientSynonymRepository extends JpaRepository<IngredientSynonym, Long> {
    
    /**
     * 동의어로 표준 재료명 찾기
     */
    Optional<IngredientSynonym> findBySynonym(String synonym);
    
    /**
     * 표준 재료명으로 모든 동의어 찾기
     */
    List<IngredientSynonym> findByStandardName(String standardName);
    
    /**
     * 모든 표준 재료명 목록 조회
     */
    @Query("SELECT DISTINCT s.standardName FROM IngredientSynonym s")
    List<String> findAllStandardNames();
    
    /**
     * 동의어 존재 여부 확인
     */
    boolean existsBySynonym(String synonym);
}
