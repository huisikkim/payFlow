package com.example.payflow.recruitment.application;

import com.example.payflow.recruitment.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 온톨로지 기반 추천 엔진
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationEngine {
    
    private final CandidateRepository candidateRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateMatchingService matchingService;
    
    /**
     * 공고에 적합한 Top N 지원자 추천
     */
    public List<CandidateRecommendation> recommendCandidatesForJob(Long jobPostingId, int topN) {
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobPostingId));
        
        List<Candidate> allCandidates = candidateRepository.findAll();
        
        return allCandidates.stream()
            .map(candidate -> {
                double score = matchingService.calculateMatchingScore(candidate, jobPosting);
                return new CandidateRecommendation(candidate, score);
            })
            .filter(rec -> rec.getScore() >= 50.0) // 50점 이상만
            .sorted(Comparator.comparingDouble(CandidateRecommendation::getScore).reversed())
            .limit(topN)
            .collect(Collectors.toList());
    }
    
    /**
     * 지원자에게 적합한 공고 추천
     */
    public List<JobRecommendation> recommendJobsForCandidate(Long candidateId, int topN) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + candidateId));
        
        List<JobPosting> activeJobs = jobPostingRepository.findActivePostings(
            JobPostingStatus.OPEN, LocalDate.now());
        
        return activeJobs.stream()
            .map(job -> {
                double score = matchingService.calculateMatchingScore(candidate, job);
                return new JobRecommendation(job, score);
            })
            .filter(rec -> rec.getScore() >= 50.0)
            .sorted(Comparator.comparingDouble(JobRecommendation::getScore).reversed())
            .limit(topN)
            .collect(Collectors.toList());
    }
    
    /**
     * 유사한 지원자 찾기 (기술 기반)
     */
    public List<Candidate> findSimilarCandidates(Long candidateId, int topN) {
        Candidate targetCandidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + candidateId));
        
        Set<Long> targetSkillIds = targetCandidate.getSkills().stream()
            .map(cs -> cs.getSkill().getId())
            .collect(Collectors.toSet());
        
        List<Candidate> allCandidates = candidateRepository.findAll();
        
        return allCandidates.stream()
            .filter(c -> !c.getId().equals(candidateId))
            .map(candidate -> {
                Set<Long> candidateSkillIds = candidate.getSkills().stream()
                    .map(cs -> cs.getSkill().getId())
                    .collect(Collectors.toSet());
                
                // Jaccard 유사도
                Set<Long> intersection = new HashSet<>(targetSkillIds);
                intersection.retainAll(candidateSkillIds);
                
                Set<Long> union = new HashSet<>(targetSkillIds);
                union.addAll(candidateSkillIds);
                
                double similarity = union.isEmpty() ? 0.0 : 
                    (double) intersection.size() / union.size();
                
                return new SimilarCandidate(candidate, similarity);
            })
            .filter(sc -> sc.getSimilarity() > 0.3)
            .sorted(Comparator.comparingDouble(SimilarCandidate::getSimilarity).reversed())
            .limit(topN)
            .map(SimilarCandidate::getCandidate)
            .collect(Collectors.toList());
    }
    
    public static class CandidateRecommendation {
        private final Candidate candidate;
        private final double score;
        
        public CandidateRecommendation(Candidate candidate, double score) {
            this.candidate = candidate;
            this.score = score;
        }
        
        public Candidate getCandidate() { return candidate; }
        public double getScore() { return score; }
    }
    
    public static class JobRecommendation {
        private final JobPosting jobPosting;
        private final double score;
        
        public JobRecommendation(JobPosting jobPosting, double score) {
            this.jobPosting = jobPosting;
            this.score = score;
        }
        
        public JobPosting getJobPosting() { return jobPosting; }
        public double getScore() { return score; }
    }
    
    private static class SimilarCandidate {
        private final Candidate candidate;
        private final double similarity;
        
        public SimilarCandidate(Candidate candidate, double similarity) {
            this.candidate = candidate;
            this.similarity = similarity;
        }
        
        public Candidate getCandidate() { return candidate; }
        public double getSimilarity() { return similarity; }
    }
}
