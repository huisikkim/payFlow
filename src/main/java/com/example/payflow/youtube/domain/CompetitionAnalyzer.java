package com.example.payflow.youtube.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * YouTube 경쟁도 분석기
 * 같은 키워드/카테고리의 경쟁 영상을 분석하여 경쟁 강도를 계산
 */
@Slf4j
@Component
public class CompetitionAnalyzer {
    
    /**
     * 경쟁도 분석
     * @param targetVideo 분석 대상 영상
     * @param competitors 경쟁 영상 목록
     * @return 경쟁도 점수 (1-100)
     */
    public CompetitionScore analyzeCompetition(YouTubeVideo targetVideo, List<YouTubeVideo> competitors) {
        if (competitors == null || competitors.isEmpty()) {
            return CompetitionScore.builder()
                    .score(50)
                    .level("보통")
                    .totalCompetitors(0)
                    .recentCompetitors(0)
                    .avgViews(0)
                    .avgEngagement(0)
                    .recommendation("경쟁 영상 데이터가 부족합니다.")
                    .build();
        }
        
        // 1. 최근 30일 내 업로드된 영상 필터링
        List<YouTubeVideo> recentCompetitors = filterRecentVideos(competitors, 30);
        
        // 2. 평균 조회수 계산
        double avgViews = recentCompetitors.stream()
                .mapToLong(v -> v.getViewCount() != null ? v.getViewCount() : 0)
                .average()
                .orElse(0);
        
        // 3. 평균 참여율 계산
        double avgEngagement = calculateAverageEngagement(recentCompetitors);
        
        // 4. 경쟁도 점수 계산 (1-100)
        int competitionScore = calculateCompetitionScore(
                recentCompetitors.size(),
                avgViews,
                avgEngagement,
                targetVideo
        );
        
        // 5. 경쟁 수준 판정
        String level = getCompetitionLevel(competitionScore);
        
        // 6. 추천 메시지
        String recommendation = getRecommendation(competitionScore, targetVideo, avgViews);
        
        log.info("경쟁도 분석 완료 - 점수: {}, 수준: {}, 경쟁자: {}/{}", 
                competitionScore, level, recentCompetitors.size(), competitors.size());
        
        return CompetitionScore.builder()
                .score(competitionScore)
                .level(level)
                .totalCompetitors(competitors.size())
                .recentCompetitors(recentCompetitors.size())
                .avgViews((long) avgViews)
                .avgEngagement(avgEngagement)
                .recommendation(recommendation)
                .build();
    }
    
    /**
     * 최근 N일 내 업로드된 영상 필터링
     */
    private List<YouTubeVideo> filterRecentVideos(List<YouTubeVideo> videos, int days) {
        ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(days);
        
        return videos.stream()
                .filter(video -> {
                    if (video.getPublishedAt() == null) return false;
                    try {
                        ZonedDateTime publishDate = ZonedDateTime.parse(
                                video.getPublishedAt(), 
                                DateTimeFormatter.ISO_DATE_TIME
                        );
                        return publishDate.isAfter(cutoffDate);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 평균 참여율 계산
     */
    private double calculateAverageEngagement(List<YouTubeVideo> videos) {
        return videos.stream()
                .mapToDouble(video -> {
                    long views = video.getViewCount() != null ? video.getViewCount() : 0;
                    long likes = video.getLikeCount() != null ? video.getLikeCount() : 0;
                    long comments = video.getCommentCount() != null ? video.getCommentCount() : 0;
                    
                    if (views == 0) return 0;
                    return ((double) (likes + comments) / views) * 100;
                })
                .average()
                .orElse(0);
    }
    
    /**
     * 경쟁도 점수 계산
     * 점수가 높을수록 경쟁이 치열함
     */
    private int calculateCompetitionScore(
            int recentCount, 
            double avgViews, 
            double avgEngagement,
            YouTubeVideo targetVideo) {
        
        int score = 0;
        
        // 1. 최근 경쟁자 수 (40점 만점)
        // 많을수록 경쟁 치열
        if (recentCount > 100) score += 40;
        else if (recentCount > 50) score += 35;
        else if (recentCount > 20) score += 25;
        else if (recentCount > 10) score += 15;
        else score += 5;
        
        // 2. 평균 조회수 (30점 만점)
        // 높을수록 경쟁 치열
        if (avgViews > 1000000) score += 30;
        else if (avgViews > 500000) score += 25;
        else if (avgViews > 100000) score += 20;
        else if (avgViews > 50000) score += 15;
        else if (avgViews > 10000) score += 10;
        else score += 5;
        
        // 3. 평균 참여율 (20점 만점)
        // 높을수록 경쟁 치열 (품질 높은 영상들)
        if (avgEngagement > 10) score += 20;
        else if (avgEngagement > 5) score += 15;
        else if (avgEngagement > 2) score += 10;
        else score += 5;
        
        // 4. 타겟 영상의 상대적 위치 (10점 만점)
        long targetViews = targetVideo.getViewCount() != null ? targetVideo.getViewCount() : 0;
        if (targetViews < avgViews * 0.5) {
            score += 10; // 평균보다 훨씬 낮음 = 경쟁 불리
        } else if (targetViews < avgViews) {
            score += 5;  // 평균보다 낮음
        } else {
            score += 0;  // 평균 이상 = 경쟁 우위
        }
        
        return Math.min(100, score);
    }
    
    /**
     * 경쟁 수준 판정
     */
    private String getCompetitionLevel(int score) {
        if (score >= 80) return "매우 높음 🔥";
        if (score >= 60) return "높음 📈";
        if (score >= 40) return "보통 ➡️";
        if (score >= 20) return "낮음 📉";
        return "매우 낮음 ✅";
    }
    
    /**
     * 추천 메시지 생성
     */
    private String getRecommendation(int score, YouTubeVideo targetVideo, double avgViews) {
        long targetViews = targetVideo.getViewCount() != null ? targetVideo.getViewCount() : 0;
        
        if (score >= 80) {
            return "경쟁이 매우 치열합니다. 차별화된 콘텐츠나 틈새 키워드를 고려하세요.";
        } else if (score >= 60) {
            return "경쟁이 높은 편입니다. 품질과 SEO 최적화에 집중하세요.";
        } else if (score >= 40) {
            if (targetViews < avgViews) {
                return "적당한 경쟁 수준입니다. 썸네일과 제목 개선으로 조회수를 높일 수 있습니다.";
            } else {
                return "적당한 경쟁 수준이며, 현재 좋은 성과를 내고 있습니다.";
            }
        } else if (score >= 20) {
            return "경쟁이 낮은 편입니다. 좋은 기회이니 꾸준히 콘텐츠를 업로드하세요.";
        } else {
            return "경쟁이 매우 낮습니다. 블루오션 키워드일 수 있으니 적극 활용하세요!";
        }
    }
    
    /**
     * 경쟁도 분석 결과
     */
    @Getter
    @Builder
    public static class CompetitionScore {
        private int score;              // 경쟁도 점수 (1-100)
        private String level;           // 경쟁 수준
        private int totalCompetitors;   // 전체 경쟁자 수
        private int recentCompetitors;  // 최근 30일 경쟁자 수
        private long avgViews;          // 평균 조회수
        private double avgEngagement;   // 평균 참여율
        private String recommendation;  // 추천 메시지
    }
}
