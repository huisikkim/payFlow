package com.example.payflow.matching.presentation;

import com.example.payflow.matching.application.MatchingService;
import com.example.payflow.matching.application.QuoteRequestService;
import com.example.payflow.matching.domain.MatchingScore;
import com.example.payflow.matching.domain.QuoteRequest;
import com.example.payflow.matching.presentation.dto.QuoteRequestDto;
import com.example.payflow.matching.presentation.dto.QuoteResponseDto;
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
    private final QuoteRequestService quoteRequestService;
    
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
    
    /**
     * 견적 요청 생성 (매장 → 유통업체)
     * POST /api/matching/quote-request
     */
    @PostMapping("/quote-request")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<QuoteRequest> createQuoteRequest(@RequestBody QuoteRequestDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        QuoteRequest quoteRequest = quoteRequestService.createQuoteRequest(
                storeId,
                request.getDistributorId(),
                request.getRequestedProducts(),
                request.getMessage()
        );
        
        return ResponseEntity.ok(quoteRequest);
    }
    
    /**
     * 매장의 견적 요청 목록 조회
     * GET /api/matching/quote-requests/store
     */
    @GetMapping("/quote-requests/store")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<QuoteRequest>> getStoreQuoteRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        List<QuoteRequest> requests = quoteRequestService.getStoreQuoteRequests(storeId);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * 유통업체의 견적 요청 목록 조회
     * GET /api/matching/quote-requests/distributor
     */
    @GetMapping("/quote-requests/distributor")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<List<QuoteRequest>> getDistributorQuoteRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        List<QuoteRequest> requests = quoteRequestService.getDistributorQuoteRequests(distributorId);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * 견적 요청 상세 조회
     * GET /api/matching/quote-request/{id}
     */
    @GetMapping("/quote-request/{id}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<QuoteRequest> getQuoteRequest(@PathVariable Long id) {
        QuoteRequest request = quoteRequestService.getQuoteRequest(id);
        return ResponseEntity.ok(request);
    }
    
    /**
     * 견적 요청 응답 (유통업체)
     * PUT /api/matching/quote-request/{id}/respond
     */
    @PutMapping("/quote-request/{id}/respond")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<QuoteRequest> respondToQuoteRequest(
            @PathVariable Long id,
            @RequestBody QuoteResponseDto response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        QuoteRequest updated = quoteRequestService.respondToQuoteRequest(
                distributorId,
                id,
                response.getStatus(),
                response.getEstimatedAmount(),
                response.getResponse()
        );
        
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 견적 요청 취소 (매장)
     * DELETE /api/matching/quote-request/{id}
     */
    @DeleteMapping("/quote-request/{id}")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<String> cancelQuoteRequest(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        quoteRequestService.cancelQuoteRequest(storeId, id);
        return ResponseEntity.ok("견적 요청이 취소되었습니다.");
    }
    
    /**
     * 견적 완료 처리 (매장)
     * PUT /api/matching/quote-request/{id}/complete
     */
    @PutMapping("/quote-request/{id}/complete")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<QuoteRequest> completeQuoteRequest(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        QuoteRequest completed = quoteRequestService.completeQuoteRequest(storeId, id);
        return ResponseEntity.ok(completed);
    }
}
