package com.example.payflow.recruitment.presentation;

import com.example.payflow.recruitment.application.RecommendationEngine;
import com.example.payflow.recruitment.presentation.dto.CandidateResponse;
import com.example.payflow.recruitment.presentation.dto.JobPostingResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recruitment/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationEngine recommendationEngine;
    
    @GetMapping("/job/{jobPostingId}/candidates")
    public ResponseEntity<List<CandidateRecommendationResponse>> recommendCandidates(
            @PathVariable Long jobPostingId,
            @RequestParam(defaultValue = "10") int topN) {
        
        List<CandidateRecommendationResponse> responses = 
            recommendationEngine.recommendCandidatesForJob(jobPostingId, topN).stream()
            .map(rec -> new CandidateRecommendationResponse(
                new CandidateResponse(rec.getCandidate()),
                rec.getScore()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/candidate/{candidateId}/jobs")
    public ResponseEntity<List<JobRecommendationResponse>> recommendJobs(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "10") int topN) {
        
        List<JobRecommendationResponse> responses = 
            recommendationEngine.recommendJobsForCandidate(candidateId, topN).stream()
            .map(rec -> new JobRecommendationResponse(
                new JobPostingResponse(rec.getJobPosting()),
                rec.getScore()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/candidate/{candidateId}/similar")
    public ResponseEntity<List<CandidateResponse>> findSimilarCandidates(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "5") int topN) {
        
        List<CandidateResponse> responses = 
            recommendationEngine.findSimilarCandidates(candidateId, topN).stream()
            .map(CandidateResponse::new)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @Getter
    public static class CandidateRecommendationResponse {
        private final CandidateResponse candidate;
        private final double matchingScore;
        
        public CandidateRecommendationResponse(CandidateResponse candidate, double matchingScore) {
            this.candidate = candidate;
            this.matchingScore = matchingScore;
        }
    }
    
    @Getter
    public static class JobRecommendationResponse {
        private final JobPostingResponse jobPosting;
        private final double matchingScore;
        
        public JobRecommendationResponse(JobPostingResponse jobPosting, double matchingScore) {
            this.jobPosting = jobPosting;
            this.matchingScore = matchingScore;
        }
    }
}
