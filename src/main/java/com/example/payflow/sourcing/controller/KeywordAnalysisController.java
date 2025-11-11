package com.example.payflow.sourcing.controller;

import com.example.payflow.sourcing.dto.KeywordAnalysisRequest;
import com.example.payflow.sourcing.dto.KeywordAnalysisResponse;
import com.example.payflow.sourcing.service.KeywordAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sourcing")
@RequiredArgsConstructor
public class KeywordAnalysisController {
    
    private final KeywordAnalysisService analysisService;
    
    /**
     * 키워드 분석 API
     */
    @PostMapping("/analyze")
    public ResponseEntity<List<KeywordAnalysisResponse>> analyzeKeyword(
            @Valid @RequestBody KeywordAnalysisRequest request) {
        
        log.info("키워드 분석 요청: {}", request.getKeyword());
        List<KeywordAnalysisResponse> results = analysisService.analyzeKeyword(request);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 블루오션 키워드 조회 API
     */
    @GetMapping("/blue-ocean")
    public ResponseEntity<List<KeywordAnalysisResponse>> getBlueOceanKeywords() {
        log.info("블루오션 키워드 조회 요청");
        List<KeywordAnalysisResponse> results = analysisService.getBlueOceanKeywords();
        return ResponseEntity.ok(results);
    }
    
    /**
     * 최근 분석 결과 조회 API
     */
    @GetMapping("/recent")
    public ResponseEntity<List<KeywordAnalysisResponse>> getRecentAnalysis(
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("최근 {}일 분석 결과 조회", days);
        List<KeywordAnalysisResponse> results = analysisService.getRecentAnalysis(days);
        return ResponseEntity.ok(results);
    }
}
