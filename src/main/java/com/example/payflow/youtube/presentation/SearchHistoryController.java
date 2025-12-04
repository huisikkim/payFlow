package com.example.payflow.youtube.presentation;

import com.example.payflow.youtube.application.SearchHistoryService;
import com.example.payflow.youtube.domain.SearchHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/youtube/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    /**
     * 내 검색 기록 조회
     * GET /api/youtube/search-history
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMySearchHistory(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "로그인이 필요합니다."
            ));
        }

        String username = authentication.getName();
        List<SearchHistory> history = searchHistoryService.getSearchHistory(username);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", history.size(),
            "history", history
        ));
    }

    /**
     * 검색 기록 삭제
     * DELETE /api/youtube/search-history/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSearchHistory(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "로그인이 필요합니다."
            ));
        }

        String username = authentication.getName();
        searchHistoryService.deleteSearchHistory(id, username);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "검색 기록이 삭제되었습니다."
        ));
    }

    /**
     * 모든 검색 기록 삭제
     * DELETE /api/youtube/search-history
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearAllSearchHistory(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "로그인이 필요합니다."
            ));
        }

        String username = authentication.getName();
        searchHistoryService.clearAllSearchHistory(username);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "모든 검색 기록이 삭제되었습니다."
        ));
    }
}
