package com.example.payflow.recruitment.application;

import com.example.payflow.recruitment.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 온톨로지 기반 지원자-공고 매칭 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateMatchingService {
    
    private final SkillMatchingEngine skillMatchingEngine;
    
    // 가중치 설정
    private static final double REQUIRED_SKILL_WEIGHT = 0.40;
    private static final double PREFERRED_SKILL_WEIGHT = 0.20;
    private static final double EXPERIENCE_WEIGHT = 0.25;
    private static final double EDUCATION_WEIGHT = 0.15;
    
    /**
     * 지원자와 공고의 매칭 점수 계산 (0-100점)
     */
    public double calculateMatchingScore(Candidate candidate, JobPosting jobPosting) {
        double requiredSkillScore = calculateRequiredSkillScore(candidate, jobPosting);
        double preferredSkillScore = calculatePreferredSkillScore(candidate, jobPosting);
        double experienceScore = calculateExperienceScore(candidate, jobPosting);
        double educationScore = calculateEducationScore(candidate, jobPosting);
        
        double totalScore = (requiredSkillScore * REQUIRED_SKILL_WEIGHT) +
                           (preferredSkillScore * PREFERRED_SKILL_WEIGHT) +
                           (experienceScore * EXPERIENCE_WEIGHT) +
                           (educationScore * EDUCATION_WEIGHT);
        
        return Math.round(totalScore * 100.0) / 100.0;
    }
    
    /**
     * 필수 기술 매칭 점수
     */
    private double calculateRequiredSkillScore(Candidate candidate, JobPosting jobPosting) {
        List<JobRequirement> requiredSkills = jobPosting.getRequirements().stream()
            .filter(r -> r.getType() == RequirementType.REQUIRED)
            .collect(Collectors.toList());
        
        if (requiredSkills.isEmpty()) {
            return 100.0;
        }
        
        double totalScore = 0.0;
        for (JobRequirement requirement : requiredSkills) {
            double bestMatch = findBestSkillMatch(candidate, requirement);
            totalScore += bestMatch;
        }
        
        return (totalScore / requiredSkills.size()) * 100.0;
    }
    
    /**
     * 우대 기술 매칭 점수
     */
    private double calculatePreferredSkillScore(Candidate candidate, JobPosting jobPosting) {
        List<JobRequirement> preferredSkills = jobPosting.getRequirements().stream()
            .filter(r -> r.getType() == RequirementType.PREFERRED)
            .collect(Collectors.toList());
        
        if (preferredSkills.isEmpty()) {
            return 100.0;
        }
        
        double totalScore = 0.0;
        for (JobRequirement requirement : preferredSkills) {
            double bestMatch = findBestSkillMatch(candidate, requirement);
            totalScore += bestMatch;
        }
        
        return (totalScore / preferredSkills.size()) * 100.0;
    }
    
    /**
     * 지원자의 기술 중 요구사항과 가장 잘 매칭되는 점수 찾기
     */
    private double findBestSkillMatch(Candidate candidate, JobRequirement requirement) {
        double bestScore = 0.0;
        
        for (CandidateSkill candidateSkill : candidate.getSkills()) {
            SkillMatchingEngine.SkillMatchResult result = 
                skillMatchingEngine.matchSkill(candidateSkill, requirement);
            bestScore = Math.max(bestScore, result.getTotalScore());
        }
        
        return bestScore;
    }
    
    /**
     * 경력 매칭 점수
     */
    private double calculateExperienceScore(Candidate candidate, JobPosting jobPosting) {
        int candidateYears = candidate.getTotalYearsOfExperience();
        
        // 공고의 최소 경력 요구사항 찾기
        OptionalInt minRequired = jobPosting.getRequirements().stream()
            .filter(r -> r.getMinYearsOfExperience() != null)
            .mapToInt(JobRequirement::getMinYearsOfExperience)
            .min();
        
        if (minRequired.isEmpty()) {
            return 100.0;
        }
        
        int requiredYears = minRequired.getAsInt();
        
        if (candidateYears >= requiredYears) {
            // 초과 경력에 대한 보너스 (최대 120점)
            double bonus = Math.min(20.0, (candidateYears - requiredYears) * 5.0);
            return Math.min(120.0, 100.0 + bonus);
        } else {
            // 부족한 경력에 대한 감점
            return (double) candidateYears / requiredYears * 100.0;
        }
    }
    
    /**
     * 학력 매칭 점수
     */
    private double calculateEducationScore(Candidate candidate, JobPosting jobPosting) {
        // 간단한 학력 매칭 (실제로는 더 복잡한 로직 가능)
        EducationLevel candidateEducation = candidate.getEducation();
        
        // 기본적으로 학사 이상이면 만점
        if (candidateEducation.getLevel() >= EducationLevel.BACHELOR.getLevel()) {
            return 100.0;
        }
        
        return candidateEducation.getLevel() * 25.0;
    }
    
    /**
     * 상세 매칭 결과
     */
    public MatchingDetail getMatchingDetail(Candidate candidate, JobPosting jobPosting) {
        return new MatchingDetail(
            calculateMatchingScore(candidate, jobPosting),
            calculateRequiredSkillScore(candidate, jobPosting),
            calculatePreferredSkillScore(candidate, jobPosting),
            calculateExperienceScore(candidate, jobPosting),
            calculateEducationScore(candidate, jobPosting),
            getSkillMatchDetails(candidate, jobPosting)
        );
    }
    
    private List<SkillMatchingEngine.SkillMatchResult> getSkillMatchDetails(
            Candidate candidate, JobPosting jobPosting) {
        List<SkillMatchingEngine.SkillMatchResult> results = new ArrayList<>();
        
        for (JobRequirement requirement : jobPosting.getRequirements()) {
            for (CandidateSkill candidateSkill : candidate.getSkills()) {
                SkillMatchingEngine.SkillMatchResult result = 
                    skillMatchingEngine.matchSkill(candidateSkill, requirement);
                if (result.getTotalScore() > 0.3) {
                    results.add(result);
                }
            }
        }
        
        return results.stream()
            .sorted(Comparator.comparingDouble(
                SkillMatchingEngine.SkillMatchResult::getTotalScore).reversed())
            .collect(Collectors.toList());
    }
    
    public static class MatchingDetail {
        private final double totalScore;
        private final double requiredSkillScore;
        private final double preferredSkillScore;
        private final double experienceScore;
        private final double educationScore;
        private final List<SkillMatchingEngine.SkillMatchResult> skillMatches;
        
        public MatchingDetail(double totalScore, double requiredSkillScore,
                            double preferredSkillScore, double experienceScore,
                            double educationScore,
                            List<SkillMatchingEngine.SkillMatchResult> skillMatches) {
            this.totalScore = totalScore;
            this.requiredSkillScore = requiredSkillScore;
            this.preferredSkillScore = preferredSkillScore;
            this.experienceScore = experienceScore;
            this.educationScore = educationScore;
            this.skillMatches = skillMatches;
        }
        
        public double getTotalScore() { return totalScore; }
        public double getRequiredSkillScore() { return requiredSkillScore; }
        public double getPreferredSkillScore() { return preferredSkillScore; }
        public double getExperienceScore() { return experienceScore; }
        public double getEducationScore() { return educationScore; }
        public List<SkillMatchingEngine.SkillMatchResult> getSkillMatches() { return skillMatches; }
    }
}
