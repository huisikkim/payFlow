package com.example.payflow.recruitment.presentation;

import com.example.payflow.recruitment.application.SkillService;
import com.example.payflow.recruitment.domain.Skill;
import com.example.payflow.recruitment.domain.SkillCategory;
import com.example.payflow.recruitment.presentation.dto.SkillRequest;
import com.example.payflow.recruitment.presentation.dto.SkillResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recruitment/skills")
@RequiredArgsConstructor
public class SkillController {
    
    private final SkillService skillService;
    
    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@RequestBody SkillRequest request) {
        Skill skill = skillService.createSkill(
            request.getName(),
            request.getCategory(),
            request.getDescription()
        );
        
        return ResponseEntity.ok(new SkillResponse(skill));
    }
    
    @PostMapping("/{skillId}/similar/{similarSkillId}")
    public ResponseEntity<Void> addSimilarSkill(
            @PathVariable Long skillId,
            @PathVariable Long similarSkillId) {
        
        skillService.addSimilarSkill(skillId, similarSkillId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkill(@PathVariable Long id) {
        Skill skill = skillService.getSkill(id);
        return ResponseEntity.ok(new SkillResponse(skill));
    }
    
    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        List<SkillResponse> responses = skillService.getAllSkills().stream()
            .map(SkillResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SkillResponse>> getSkillsByCategory(@PathVariable SkillCategory category) {
        List<SkillResponse> responses = skillService.getSkillsByCategory(category).stream()
            .map(SkillResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<SkillResponse>> searchSkills(@RequestParam String keyword) {
        List<SkillResponse> responses = skillService.searchSkills(keyword).stream()
            .map(SkillResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{skillId}/similar")
    public ResponseEntity<List<SkillResponse>> getSimilarSkills(@PathVariable Long skillId) {
        List<SkillResponse> responses = skillService.getSimilarSkills(skillId).stream()
            .map(SkillResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
