package com.example.payflow.recruitment.application;

import com.example.payflow.recruitment.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {
    
    private final JobApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateMatchingService matchingService;
    
    @Transactional
    public JobApplication applyForJob(Long candidateId, Long jobPostingId, String coverLetter) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + candidateId));
        
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobPostingId));
        
        // 중복 지원 체크
        applicationRepository.findByJobPostingAndCandidate(jobPostingId, candidateId)
            .ifPresent(app -> {
                throw new IllegalArgumentException("이미 지원한 공고입니다.");
            });
        
        // 공고 상태 체크
        if (jobPosting.getStatus() != JobPostingStatus.OPEN) {
            throw new IllegalArgumentException("지원할 수 없는 공고입니다.");
        }
        
        // 온톨로지 기반 매칭 스코어 자동 계산
        double matchingScore = matchingService.calculateMatchingScore(candidate, jobPosting);
        
        JobApplication application = JobApplication.create(
            candidate, jobPosting, matchingScore, coverLetter
        );
        
        return applicationRepository.save(application);
    }
    
    @Transactional
    public void updateApplicationStatus(Long applicationId, ApplicationStatus status, String notes) {
        JobApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("지원을 찾을 수 없습니다: " + applicationId));
        
        application.updateStatus(status, notes);
    }
    
    @Transactional
    public void recalculateMatchingScore(Long applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("지원을 찾을 수 없습니다: " + applicationId));
        
        double newScore = matchingService.calculateMatchingScore(
            application.getCandidate(),
            application.getJobPosting()
        );
        
        application.updateMatchingScore(newScore);
    }
    
    public JobApplication getApplication(Long id) {
        return applicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("지원을 찾을 수 없습니다: " + id));
    }
    
    public List<JobApplication> getApplicationsByJobPosting(Long jobPostingId) {
        return applicationRepository.findByJobPostingId(jobPostingId);
    }
    
    public List<JobApplication> getApplicationsByJobPostingOrderedByScore(Long jobPostingId) {
        return applicationRepository.findByJobPostingIdOrderByMatchingScoreDesc(jobPostingId);
    }
    
    public List<JobApplication> getApplicationsByCandidate(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }
    
    public List<JobApplication> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }
    
    public CandidateMatchingService.MatchingDetail getMatchingDetail(Long applicationId) {
        JobApplication application = getApplication(applicationId);
        return matchingService.getMatchingDetail(
            application.getCandidate(),
            application.getJobPosting()
        );
    }
}
