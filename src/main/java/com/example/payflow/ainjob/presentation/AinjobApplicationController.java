package com.example.payflow.ainjob.presentation;

import com.example.payflow.ainjob.application.ApplicationTrackingService;
import com.example.payflow.ainjob.application.dto.ApplicationCreateRequest;
import com.example.payflow.ainjob.application.dto.ApplicationResponse;
import com.example.payflow.ainjob.application.dto.ApplicationStatusChangeRequest;
import com.example.payflow.ainjob.domain.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ainjob/applications")
@RequiredArgsConstructor
public class AinjobApplicationController {
    
    private final ApplicationTrackingService applicationTrackingService;
    
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@RequestBody ApplicationCreateRequest request) {
        ApplicationResponse response = applicationTrackingService.createApplication(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable Long id) {
        ApplicationResponse response = applicationTrackingService.getApplication(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/applicant/{applicantId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByApplicant(@PathVariable Long applicantId) {
        List<ApplicationResponse> responses = applicationTrackingService.getApplicationsByApplicant(applicantId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/job-posting/{jobPostingId}")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByJobPosting(
            @PathVariable Long jobPostingId,
            @PageableDefault(size = 20, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ApplicationResponse> responses = applicationTrackingService.getApplicationsByJobPosting(jobPostingId, pageable);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/job-posting/{jobPostingId}/status/{status}")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByJobPostingAndStatus(
            @PathVariable Long jobPostingId,
            @PathVariable ApplicationStatus status,
            @PageableDefault(size = 20, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ApplicationResponse> responses = applicationTrackingService.getApplicationsByJobPostingAndStatus(
            jobPostingId, status, pageable
        );
        return ResponseEntity.ok(responses);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> changeApplicationStatus(
            @PathVariable Long id,
            @RequestBody ApplicationStatusChangeRequest request) {
        ApplicationResponse response = applicationTrackingService.changeApplicationStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
