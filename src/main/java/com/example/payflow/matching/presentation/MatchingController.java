package com.example.payflow.matching.presentation;

import com.example.payflow.matching.application.MatchingService;
import com.example.payflow.matching.domain.MatchingScore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {
    
    private final MatchingService matchingService;
    
    /**
     * 맞춤 유통업체 추천
     * GET /api/matching/recommend
     */
    @GetMapping("/recommend")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<MatchingScore>> recommendDistributors(
            @RequestParam(defaultValue = "10") int limit) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        List<MatchingScore> recommendations = matchingService.recommendDistributors(storeId, limit);
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * 품목별 유통업체 검색
     * GET /api/matching/search/product?keyword=쌀
     */
    @GetMapping("/search/product")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<MatchingScore>> searchByProduct(@RequestParam String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        List<MatchingScore> results = matchingService.searchDistributorsByProduct(storeId, keyword);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 지역별 유통업체 검색
     * GET /api/matching/search/region?keyword=서울
     */
    @GetMapping("/search/region")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<MatchingScore>> searchByRegion(@RequestParam String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        List<MatchingScore> results = matchingService.searchDistributorsByRegion(storeId, keyword);
        return ResponseEntity.ok(results);
    }
}
