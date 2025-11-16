package com.example.payflow.recruitment.presentation;

import com.example.payflow.recruitment.application.ApplicationService;
import com.example.payflow.recruitment.domain.ApplicationStatus;
import com.example.payflow.recruitment.domain.JobApplication;
import com.example.payflow.recruitment.presentation.dto.JobApplicationRequest;
import com.example.payflow.recruitment.presentation.dto.JobApplicationResponse;
import com.example.payflow.recruitment.presentation.dto.MatchingDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recruitment/applications")
@RequiredArgsConstructor
public class ApplicationController {
    
    private final ApplicationService applicationService;
    
    @PostMapping
    public ResponseEntity<JobApplicationResponse> applyForJob(@RequestBody JobApplicationRequest request) {
        JobApplication application = applicationService.applyForJob(
            request.getCandidateId(),
            request.getJobPostingId(),
            request.getCoverLetter()
        );
        
        return ResponseEntity.ok(new JobApplicationResponse(application));
    }
    
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String notes) {
        
        applicationService.updateApplicationStatus(applicationId, status, notes);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{applicationId}/recalculate-score")
    public ResponseEntity<Void> recalculateScore(@PathVariable Long applicationId) {
        applicationService.recalculateMatchingScore(applicationId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getApplication(@PathVariable Long id) {
        JobApplication application = applicationService.getApplication(id);
        return ResponseEntity.ok(new JobApplicationResponse(application));
    }
    
    @GetMapping("/{id}/matching-detail")
    public ResponseEntity<MatchingDetailResponse> getMatchingDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
            new MatchingDetailResponse(applicationService.getMatchingDetail(id))
        );
    }
    
    @GetMapping("/job/{jobPostingId}")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsByJob(
            @PathVariable Long jobPostingId) {
        List<JobApplicationResponse> responses = 
            applicationService.getApplicationsByJobPostingOrderedByScore(jobPostingId).stream()
            .map(JobApplicationResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsByCandidate(
            @PathVariable Long candidateId) {
        List<JobApplicationResponse> responses = 
            applicationService.getApplicationsByCandidate(candidateId).stream()
            .map(JobApplicationResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
