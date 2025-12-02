package com.example.payflow.specification.presentation;

import com.example.payflow.specification.application.RuleBasedMatchingService;
import com.example.payflow.specification.domain.IngredientSynonym;
import com.example.payflow.specification.presentation.dto.IngredientMatchResult;
import com.example.payflow.specification.presentation.dto.SynonymRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 재료 매칭 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientMatchingController {
    
    private final RuleBasedMatchingService matchingService;
    
    /**
     * 단일 OCR 텍스트 매칭
     */
    @PostMapping("/match")
    public ResponseEntity<IngredientMatchResult> matchIngredient(@RequestBody Map<String, String> request) {
        String ocrText = request.get("ocrText");
        log.info("재료 매칭 요청: {}", ocrText);
        
        IngredientMatchResult result = matchingService.matchIngredient(ocrText);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 여러 OCR 텍스트 일괄 매칭
     */
    @PostMapping("/match/batch")
    public ResponseEntity<List<IngredientMatchResult>> matchIngredients(
            @RequestBody Map<String, List<String>> request) {
        List<String> ocrTexts = request.get("ocrTexts");
        log.info("일괄 매칭 요청: {} 건", ocrTexts.size());
        
        List<IngredientMatchResult> results = matchingService.matchIngredients(ocrTexts);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 동의어 추가 (관리자)
     */
    @PostMapping("/synonyms")
    public ResponseEntity<IngredientSynonym> addSynonym(@RequestBody SynonymRequest request) {
        log.info("동의어 추가: {} -> {}", request.getSynonym(), request.getStandardName());
        
        IngredientSynonym synonym = matchingService.addSynonym(
            request.getStandardName(),
            request.getSynonym(),
            request.getSimilarityScore()
        );
        
        return ResponseEntity.ok(synonym);
    }
    
    /**
     * 표준 재료명의 동의어 목록 조회
     */
    @GetMapping("/synonyms/{standardName}")
    public ResponseEntity<List<IngredientSynonym>> getSynonyms(@PathVariable String standardName) {
        List<IngredientSynonym> synonyms = matchingService.getSynonyms(standardName);
        return ResponseEntity.ok(synonyms);
    }
    
    /**
     * 모든 표준 재료명 목록
     */
    @GetMapping("/standard-names")
    public ResponseEntity<List<String>> getAllStandardNames() {
        List<String> names = matchingService.getAllStandardNames();
        return ResponseEntity.ok(names);
    }
}
