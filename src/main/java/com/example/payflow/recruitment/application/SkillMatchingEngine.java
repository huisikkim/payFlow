package com.example.payflow.recruitment.application;

import com.example.payflow.recruitment.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 온톨로지 기반 기술 매칭 엔진
 * 규칙 기반 추론으로 기술 유사도를 계산
 */
@Service
@RequiredArgsConstructor
public class SkillMatchingEngine {
    
    private final SkillRepository skillRepository;
    
    /**
     * 두 기술 간의 유사도 계산 (0.0 ~ 1.0)
     */
    public double calculateSimilarity(Skill skill1, Skill skill2) {
        // 정확 매칭
        if (skill1.getId().equals(skill2.getId())) {
            return 1.0;
        }
        
        // 온톨로지 관계 기반 유사도
        if (skill1.getSimilarSkills().contains(skill2)) {
            return skill1.getDefaultSimilarityScore();
        }
        
        // 같은 카테고리면 기본 유사도
        if (skill1.getCategory() == skill2.getCategory()) {
            return 0.5;
        }
        
        // 관련 없음
        return 0.0;
    }
    
    /**
     * 지원자 기술과 요구사항 기술 매칭
     */
    public SkillMatchResult matchSkill(CandidateSkill candidateSkill, JobRequirement requirement) {
        double similarityScore = calculateSimilarity(candidateSkill.getSkill(), requirement.getSkill());
        
        // 숙련도 매칭
        double proficiencyScore = calculateProficiencyScore(
            candidateSkill.getProficiencyLevel(),
            requirement.getMinProficiency()
        );
        
        // 경력 매칭
        double experienceScore = calculateExperienceScore(
            candidateSkill.getYearsOfExperience(),
            requirement.getMinYearsOfExperience()
        );
        
        // 종합 점수 (가중 평균)
        double totalScore = (similarityScore * 0.5) + (proficiencyScore * 0.3) + (experienceScore * 0.2);
        
        return new SkillMatchResult(
            candidateSkill.getSkill(),
            requirement.getSkill(),
            similarityScore,
            proficiencyScore,
            experienceScore,
            totalScore
        );
    }
    
    private double calculateProficiencyScore(ProficiencyLevel candidateLevel, ProficiencyLevel requiredLevel) {
        if (requiredLevel == null) {
            return 1.0;
        }
        
        int candidateLevelValue = candidateLevel.getLevel();
        int requiredLevelValue = requiredLevel.getLevel();
        
        if (candidateLevelValue >= requiredLevelValue) {
            return 1.0;
        } else {
            return (double) candidateLevelValue / requiredLevelValue;
        }
    }
    
    private double calculateExperienceScore(Integer candidateYears, Integer requiredYears) {
        if (requiredYears == null || requiredYears == 0) {
            return 1.0;
        }
        
        if (candidateYears == null) {
            return 0.0;
        }
        
        if (candidateYears >= requiredYears) {
            return 1.0;
        } else {
            return (double) candidateYears / requiredYears;
        }
    }
    
    /**
     * 기술 매칭 결과
     */
    public static class SkillMatchResult {
        private final Skill candidateSkill;
        private final Skill requiredSkill;
        private final double similarityScore;
        private final double proficiencyScore;
        private final double experienceScore;
        private final double totalScore;
        
        public SkillMatchResult(Skill candidateSkill, Skill requiredSkill,
                               double similarityScore, double proficiencyScore,
                               double experienceScore, double totalScore) {
            this.candidateSkill = candidateSkill;
            this.requiredSkill = requiredSkill;
            this.similarityScore = similarityScore;
            this.proficiencyScore = proficiencyScore;
            this.experienceScore = experienceScore;
            this.totalScore = totalScore;
        }
        
        public Skill getCandidateSkill() { return candidateSkill; }
        public Skill getRequiredSkill() { return requiredSkill; }
        public double getSimilarityScore() { return similarityScore; }
        public double getProficiencyScore() { return proficiencyScore; }
        public double getExperienceScore() { return experienceScore; }
        public double getTotalScore() { return totalScore; }
    }
}
