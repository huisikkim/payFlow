package com.example.payflow.recruitment.presentation;

import com.example.payflow.recruitment.application.CandidateService;
import com.example.payflow.recruitment.domain.Candidate;
import com.example.payflow.recruitment.domain.ProficiencyLevel;
import com.example.payflow.recruitment.presentation.dto.CandidateRequest;
import com.example.payflow.recruitment.presentation.dto.CandidateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recruitment/candidates")
@RequiredArgsConstructor
public class CandidateController {
    
    private final CandidateService candidateService;
    
    @PostMapping
    public ResponseEntity<CandidateResponse> createCandidate(@RequestBody CandidateRequest request) {
        Candidate candidate = candidateService.createCandidate(
            request.getName(),
            request.getEmail(),
            request.getPhone(),
            request.getEducation(),
            request.getUniversity(),
            request.getMajor()
        );
        
        return ResponseEntity.ok(new CandidateResponse(candidate));
    }
    
    @PostMapping("/{candidateId}/skills")
    public ResponseEntity<Void> addSkill(
            @PathVariable Long candidateId,
            @RequestParam Long skillId,
            @RequestParam ProficiencyLevel proficiencyLevel,
            @RequestParam(required = false) Integer yearsOfExperience,
            @RequestParam(required = false) String description) {
        
        candidateService.addSkillToCandidate(
            candidateId, skillId, proficiencyLevel, yearsOfExperience, description
        );
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{candidateId}/experiences")
    public ResponseEntity<Void> addExperience(
            @PathVariable Long candidateId,
            @RequestParam String company,
            @RequestParam String position,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean currentlyWorking,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String achievements) {
        
        candidateService.addExperienceToCandidate(
            candidateId, company, position, startDate, endDate,
            currentlyWorking, description, achievements
        );
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getCandidate(@PathVariable Long id) {
        Candidate candidate = candidateService.getCandidate(id);
        return ResponseEntity.ok(new CandidateResponse(candidate));
    }
    
    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getAllCandidates() {
        List<CandidateResponse> responses = candidateService.getAllCandidates().stream()
            .map(CandidateResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/skill/{skillId}")
    public ResponseEntity<List<CandidateResponse>> getCandidatesBySkill(@PathVariable Long skillId) {
        List<CandidateResponse> responses = candidateService.getCandidatesBySkill(skillId).stream()
            .map(CandidateResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
