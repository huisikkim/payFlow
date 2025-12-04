package com.example.payflow.youtube.presentation;

import com.example.payflow.youtube.application.YouTubeService;
import com.example.payflow.youtube.domain.YouTubeVideo;
import com.example.payflow.youtube.domain.YouTubeVideoStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YouTubeController {

    private final YouTubeService youTubeService;
    private final com.example.payflow.youtube.application.SearchHistoryService searchHistoryService;

    /**
     * 한국 인기 급상승 영상 목록 조회
     * GET /api/youtube/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getKoreanPopularVideos() {
        List<YouTubeVideo> videos = youTubeService.getKoreanPopularVideos();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", videos.size(),
            "videos", videos
        ));
    }

    /**
     * 특정 국가의 인기 급상승 영상 목록 조회 (페이지네이션 지원)
     * GET /api/youtube/popular/{regionCode}?maxResults=25&pageToken=xxx
     */
    @GetMapping("/popular/{regionCode}")
    public ResponseEntity<Map<String, Object>> getPopularVideos(
            @PathVariable String regionCode,
            @RequestParam(defaultValue = "25") int maxResults,
            @RequestParam(required = false) String pageToken) {
        try {
            Map<String, Object> result = youTubeService.getPopularVideosWithPagination(regionCode, maxResults, pageToken);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("YouTube API 호출 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "YouTube API 호출 실패: " + e.getMessage(),
                "hint", "Google Cloud Console에서 유효한 API 키를 발급받아 YOUTUBE_API_KEY 환경변수로 설정하세요."
            ));
        }
    }

    /**
     * 특정 영상의 통계 정보 조회
     * GET /api/youtube/statistics/{videoId}
     */
    @GetMapping("/statistics/{videoId}")
    public ResponseEntity<Map<String, Object>> getVideoStatistics(@PathVariable String videoId) {
        YouTubeVideoStatistics stats = youTubeService.getVideoStatistics(videoId);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
            "success", true,
            "statistics", stats
        ));
    }

    /**
     * 여러 영상의 통계 정보 일괄 조회
     * POST /api/youtube/statistics/batch
     * Body: { "videoIds": ["id1", "id2", ...] }
     */
    @PostMapping("/statistics/batch")
    public ResponseEntity<Map<String, Object>> getMultipleVideoStatistics(
            @RequestBody Map<String, List<String>> request) {
        List<String> videoIds = request.get("videoIds");
        if (videoIds == null || videoIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "videoIds is required"
            ));
        }
        
        List<YouTubeVideoStatistics> statsList = youTubeService.getMultipleVideoStatistics(videoIds);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", statsList.size(),
            "statistics", statsList
        ));
    }

    /**
     * 조회수 기준 상위 영상 조회
     * GET /api/youtube/top-viewed?regionCode=KR&topN=10
     */
    @GetMapping("/top-viewed")
    public ResponseEntity<Map<String, Object>> getTopViewedVideos(
            @RequestParam(defaultValue = "KR") String regionCode,
            @RequestParam(defaultValue = "10") int topN) {
        List<YouTubeVideo> videos = youTubeService.getTopViewedVideos(regionCode, topN);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "regionCode", regionCode,
            "count", videos.size(),
            "videos", videos
        ));
    }

    /**
     * 좋아요 기준 상위 영상 조회
     * GET /api/youtube/top-liked?regionCode=KR&topN=10
     */
    @GetMapping("/top-liked")
    public ResponseEntity<Map<String, Object>> getTopLikedVideos(
            @RequestParam(defaultValue = "KR") String regionCode,
            @RequestParam(defaultValue = "10") int topN) {
        List<YouTubeVideo> videos = youTubeService.getTopLikedVideos(regionCode, topN);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "regionCode", regionCode,
            "count", videos.size(),
            "videos", videos
        ));
    }

    /**
     * 키워드로 영상 검색
     * GET /api/youtube/search?q=검색어&maxResults=25
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchVideos(
            @RequestParam String q,
            @RequestParam(defaultValue = "25") int maxResults,
            org.springframework.security.core.Authentication authentication) {
        try {
            List<YouTubeVideo> videos = youTubeService.searchVideos(q, maxResults);
            
            // 로그인한 사용자의 검색 기록 저장
            if (authentication != null && authentication.isAuthenticated()) {
                try {
                    String username = authentication.getName();
                    log.info("검색 기록 저장 시도 - username: {}, query: {}, resultCount: {}", username, q, videos.size());
                    searchHistoryService.saveSearchHistory(username, q, videos.size());
                    log.info("검색 기록 저장 성공");
                } catch (Exception e) {
                    log.error("검색 기록 저장 실패", e);
                }
            } else {
                log.info("비로그인 사용자 검색 - 검색 기록 저장 안 함");
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "query", q,
                "count", videos.size(),
                "videos", videos
            ));
        } catch (Exception e) {
            log.error("YouTube 검색 API 호출 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "YouTube 검색 API 호출 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 영상 수익 예측
     * GET /api/youtube/revenue/{videoId}
     */
    @GetMapping("/revenue/{videoId}")
    public ResponseEntity<Map<String, Object>> estimateRevenue(@PathVariable String videoId) {
        try {
            com.example.payflow.youtube.domain.RevenueEstimate estimate = 
                    youTubeService.estimateRevenue(videoId);
            
            if (estimate == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "videoId", videoId,
                "revenue", estimate
            ));
        } catch (Exception e) {
            log.error("수익 예측 실패 - videoId: {}", videoId, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "수익 예측 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 월 수익 시뮬레이션
     * GET /api/youtube/revenue/{videoId}/monthly?videosPerMonth=10
     */
    @GetMapping("/revenue/{videoId}/monthly")
    public ResponseEntity<Map<String, Object>> simulateMonthlyRevenue(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "10") int videosPerMonth) {
        try {
            com.example.payflow.youtube.domain.MonthlyRevenueSimulation simulation = 
                    youTubeService.simulateMonthlyRevenue(videoId, videosPerMonth);
            
            if (simulation == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "videoId", videoId,
                "simulation", simulation
            ));
        } catch (Exception e) {
            log.error("월 수익 시뮬레이션 실패 - videoId: {}", videoId, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "월 수익 시뮬레이션 실패: " + e.getMessage()
            ));
        }
    }

}
