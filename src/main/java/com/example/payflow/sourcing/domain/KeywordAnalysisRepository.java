package com.example.payflow.sourcing.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordAnalysisRepository extends JpaRepository<KeywordAnalysis, Long> {
    
    Optional<KeywordAnalysis> findByKeywordAndPlatform(String keyword, String platform);
    
    List<KeywordAnalysis> findByPlatformOrderByProfitabilityScoreDesc(String platform);
    
    List<KeywordAnalysis> findByMarketTypeOrderByProfitabilityScoreDesc(String marketType);
    
    @Query("SELECT k FROM KeywordAnalysis k WHERE k.analyzedAt > :since ORDER BY k.profitabilityScore DESC")
    List<KeywordAnalysis> findRecentAnalysis(LocalDateTime since);
}
