package com.example.payflow.ainjob.presentation;

import com.example.payflow.ainjob.application.JobPostingService;
import com.example.payflow.ainjob.application.dto.JobPostingCreateRequest;
import com.example.payflow.ainjob.application.dto.JobPostingResponse;
import com.example.payflow.ainjob.domain.JobPostingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ainjob/job-postings")
@RequiredArgsConstructor
public class AinjobJobPostingController {
    
    private final JobPostingService jobPostingService;
    private final com.example.payflow.ainjob.application.ApplicantMatchingService applicantMatchingService;
    
    @PostMapping
    public ResponseEntity<JobPostingResponse> createJobPosting(@RequestBody JobPostingCreateRequest request) {
        JobPostingResponse response = jobPostingService.createJobPosting(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobPostingResponse> getJobPosting(@PathVariable Long id) {
        JobPostingResponse response = jobPostingService.getJobPosting(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<JobPostingResponse>> getAllJobPostings() {
        List<JobPostingResponse> responses = jobPostingService.getAllJobPostings();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<JobPostingResponse>> getJobPostingsByCompany(@PathVariable Long companyId) {
        List<JobPostingResponse> responses = jobPostingService.getJobPostingsByCompany(companyId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<JobPostingResponse>> getJobPostingsByStatus(@PathVariable JobPostingStatus status) {
        List<JobPostingResponse> responses = jobPostingService.getJobPostingsByStatus(status);
        return ResponseEntity.ok(responses);
    }
    
    @PatchMapping("/{id}/open")
    public ResponseEntity<Void> openJobPosting(@PathVariable Long id) {
        jobPostingService.openJobPosting(id);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> closeJobPosting(@PathVariable Long id) {
        jobPostingService.closeJobPosting(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/qualified-applicants")
    public ResponseEntity<List<com.example.payflow.ainjob.application.dto.QualifiedApplicantResponse>> getQualifiedApplicants(
            @PathVariable Long id) {
        List<com.example.payflow.ainjob.application.dto.QualifiedApplicantResponse> responses = 
            applicantMatchingService.getQualifiedApplicants(id);
        return ResponseEntity.ok(responses);
    }
}
