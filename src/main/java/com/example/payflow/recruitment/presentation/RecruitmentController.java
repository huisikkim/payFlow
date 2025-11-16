package com.example.payflow.recruitment.presentation;

import com.example.payflow.recruitment.application.RecruitmentService;
import com.example.payflow.recruitment.domain.JobPosting;
import com.example.payflow.recruitment.domain.ProficiencyLevel;
import com.example.payflow.recruitment.domain.RequirementType;
import com.example.payflow.recruitment.presentation.dto.JobPostingRequest;
import com.example.payflow.recruitment.presentation.dto.JobPostingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recruitment/jobs")
@RequiredArgsConstructor
public class RecruitmentController {
    
    private final RecruitmentService recruitmentService;
    
    @PostMapping
    public ResponseEntity<JobPostingResponse> createJobPosting(@RequestBody JobPostingRequest request) {
        JobPosting jobPosting = recruitmentService.createJobPosting(
            request.getTitle(),
            request.getDescription(),
            request.getDepartmentId(),
            request.getPosition(),
            request.getHeadcount(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        return ResponseEntity.ok(new JobPostingResponse(jobPosting));
    }
    
    @PostMapping("/{jobPostingId}/requirements")
    public ResponseEntity<Void> addRequirement(
            @PathVariable Long jobPostingId,
            @RequestParam Long skillId,
            @RequestParam RequirementType type,
            @RequestParam(required = false) ProficiencyLevel minProficiency,
            @RequestParam(required = false) Integer minYearsOfExperience,
            @RequestParam(required = false) String description) {
        
        recruitmentService.addRequirement(
            jobPostingId, skillId, type, minProficiency, minYearsOfExperience, description
        );
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{jobPostingId}/publish")
    public ResponseEntity<Void> publishJobPosting(@PathVariable Long jobPostingId) {
        recruitmentService.publishJobPosting(jobPostingId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{jobPostingId}/close")
    public ResponseEntity<Void> closeJobPosting(@PathVariable Long jobPostingId) {
        recruitmentService.closeJobPosting(jobPostingId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobPostingResponse> getJobPosting(@PathVariable Long id) {
        JobPosting jobPosting = recruitmentService.getJobPosting(id);
        return ResponseEntity.ok(new JobPostingResponse(jobPosting));
    }
    
    @GetMapping
    public ResponseEntity<List<JobPostingResponse>> getAllJobPostings() {
        List<JobPostingResponse> responses = recruitmentService.getAllJobPostings().stream()
            .map(JobPostingResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<JobPostingResponse>> getActiveJobPostings() {
        List<JobPostingResponse> responses = recruitmentService.getActiveJobPostings().stream()
            .map(JobPostingResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<JobPostingResponse>> getJobPostingsByDepartment(
            @PathVariable Long departmentId) {
        List<JobPostingResponse> responses = recruitmentService.getJobPostingsByDepartment(departmentId).stream()
            .map(JobPostingResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
