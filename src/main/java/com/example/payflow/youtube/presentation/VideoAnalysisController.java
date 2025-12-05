package com.example.payflow.youtube.presentation;

import com.example.payflow.youtube.application.VideoAnalysisService;
import com.example.payflow.youtube.domain.VideoAnalysisReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * YouTube 영상 분석 API
 * URL 입력 → 수익/조회수/경쟁 분석 자동 리포트
 */
@Slf4j
@RestController
@RequestMapping("/api/youtube/analysis")
@RequiredArgsConstructor
public class VideoAnalysisController {
    
    private final VideoAnalysisService videoAnalysisService;
    
    /**
     * YouTube URL에서 videoId 추출
     * GET /api/youtube/analysis/extract?url=https://youtube.com/watch?v=xxx
     */
    @GetMapping("/extract")
    public ResponseEntity<Map<String, Object>> extractVideoId(@RequestParam String url) {
        try {
            String videoId = videoAnalysisService.extractVideoId(url);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "videoId", videoId,
                "url", url
            ));
        } catch (Exception e) {
            log.error("VideoId 추출 실패: {}", url, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 영상 종합 분석 리포트
     * GET /api/youtube/analysis/{videoId}
     * 
     * 응답 예시:
     * {
     *   "videoTitle": "How to Learn Spring Boot",
     *   "channel": "DevMaster",
     *   "currentViews": 12004,
     *   "predictedViews": 34000,
     *   "predictedRevenue": 52300,
     *   "competitionScore": 73,
     *   "tags": ["spring", "backend", "java"],
     *   "engagementRate": 4.2,
     *   "recommendedTitles": [...]
     * }
     */
    @GetMapping("/{videoId}")
    public ResponseEntity<Map<String, Object>> analyzeVideo(@PathVariable String videoId) {
        try {
            log.info("영상 분석 요청 - videoId: {}", videoId);
            
            VideoAnalysisReport report = videoAnalysisService.analyzeVideo(videoId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "report", report
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("영상 분석 실패 - videoId: {}, error: {}", videoId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("영상 분석 중 오류 발생 - videoId: {}", videoId, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "영상 분석 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * URL로 직접 분석
     * POST /api/youtube/analysis/url
     * Body: { "url": "https://youtube.com/watch?v=xxx" }
     */
    @PostMapping("/url")
    public ResponseEntity<Map<String, Object>> analyzeByUrl(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");
            if (url == null || url.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "URL이 필요합니다."
                ));
            }
            
            log.info("URL 기반 영상 분석 요청 - url: {}", url);
            
            // 1. URL에서 videoId 추출
            String videoId = videoAnalysisService.extractVideoId(url);
            
            // 2. 영상 분석
            VideoAnalysisReport report = videoAnalysisService.analyzeVideo(videoId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "videoId", videoId,
                "report", report
            ));
            
        } catch (Exception e) {
            log.error("URL 기반 영상 분석 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "영상 분석 실패: " + e.getMessage()
            ));
        }
    }
}
