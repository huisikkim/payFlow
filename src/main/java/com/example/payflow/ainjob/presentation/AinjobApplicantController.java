package com.example.payflow.ainjob.presentation;

import com.example.payflow.ainjob.application.ApplicantService;
import com.example.payflow.ainjob.application.dto.ApplicantCreateRequest;
import com.example.payflow.ainjob.application.dto.ApplicantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ainjob/applicants")
@RequiredArgsConstructor
public class AinjobApplicantController {
    
    private final ApplicantService applicantService;
    
    @PostMapping
    public ResponseEntity<ApplicantResponse> createApplicant(@RequestBody ApplicantCreateRequest request) {
        ApplicantResponse response = applicantService.createApplicant(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApplicantResponse> getApplicant(@PathVariable Long id) {
        ApplicantResponse response = applicantService.getApplicant(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ApplicantResponse>> getAllApplicants() {
        List<ApplicantResponse> responses = applicantService.getAllApplicants();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<ApplicantResponse> getApplicantByEmail(@PathVariable String email) {
        ApplicantResponse response = applicantService.getApplicantByEmail(email);
        return ResponseEntity.ok(response);
    }
}
