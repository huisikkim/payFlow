package com.example.payflow.youtube.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 경쟁 영상 분석기 (강화 버전)
 */
@Slf4j
@Component
public class CompetitorAnalyzer {
    
    /**
     * 경쟁 영상 상세 분석
     */
    public CompetitorAnalysis analyzeDetailed(YouTubeVideo targetVideo, List<YouTubeVideo> competitors) {
        if (competitors == null || competitors.isEmpty()) {
            return CompetitorAnalysis.builder()
                .totalCompetitors(0)
                .top20Count(0)
                .viewsMedian(0L)
                .viewsAverage(0L)
                .viewsMin(0L)
                .viewsMax(0L)
                .engagementMedian(0.0)
                .engagementAverage(0.0)
                .growthPatterns(new ArrayList<>())
                .topCompetitors(new ArrayList<>())
                .build();
        }
        
        // Top 20 필터링
        List<YouTubeVideo> top20 = competitors.stream()
            .filter(v -> v.getViewCount() != null)
            .sorted(Comparator.comparing(YouTubeVideo::getViewCount).reversed())
            .limit(20)
            .collect(Collectors.toList());
        
        // 조회수 통계
        List<Long> viewCounts = top20.stream()
            .map(YouTubeVideo::getViewCount)
            .sorted()
            .collect(Collectors.toList());
        
        long viewsMedian = calculateMedian(viewCounts);
        long viewsAverage = (long) viewCounts.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0);
        long viewsMin = viewCounts.isEmpty() ? 0 : viewCounts.get(0);
        long viewsMax = viewCounts.isEmpty() ? 0 : viewCounts.get(viewCounts.size() - 1);
        
        // 참여도 통계
        List<Double> engagementRates = top20.stream()
            .map(this::calculateEngagementRate)
            .sorted()
            .collect(Collectors.toList());
        
        double engagementMedian = calculateMedianDouble(engagementRates);
        double engagementAverage = engagementRates.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
        
        // 성장 패턴 분석
        List<CompetitorAnalysis.GrowthPattern> growthPatterns = analyzeGrowthPatterns(top20);
        
        // 상위 경쟁 영상 정보
        List<CompetitorAnalysis.CompetitorVideo> topCompetitors = top20.stream()
            .limit(10)
            .map(v -> CompetitorAnalysis.CompetitorVideo.builder()
                .videoId(v.getVideoId())
                .title(v.getTitle())
                .channel(v.getChannelTitle())
                .views(v.getViewCount())
                .likes(v.getLikeCount())
                .daysOld(calculateDaysOld(v.getPublishedAt()))
                .engagementRate(calculateEngagementRate(v))
                .build())
            .collect(Collectors.toList());
        
        return CompetitorAnalysis.builder()
            .totalCompetitors(competitors.size())
            .top20Count(top20.size())
            .viewsMedian(viewsMedian)
            .viewsAverage(viewsAverage)
            .viewsMin(viewsMin)
            .viewsMax(viewsMax)
            .engagementMedian(engagementMedian)
            .engagementAverage(engagementAverage)
            .growthPatterns(growthPatterns)
            .topCompetitors(topCompetitors)
            .build();
    }
    
    /**
     * 성장 패턴 분석 (업로드 시간 대비 조회수 증가)
     */
    private List<CompetitorAnalysis.GrowthPattern> analyzeGrowthPatterns(List<YouTubeVideo> videos) {
        return videos.stream()
            .filter(v -> v.getPublishedAt() != null && v.getViewCount() != null)
            .map(v -> {
                int daysOld = calculateDaysOld(v.getPublishedAt());
                long viewsPerDay = daysOld > 0 ? v.getViewCount() / daysOld : v.getViewCount();
                String growthRate = categorizeGrowthRate(viewsPerDay);
                
                return CompetitorAnalysis.GrowthPattern.builder()
                    .videoId(v.getVideoId())
                    .title(v.getTitle())
                    .views(v.getViewCount())
                    .daysOld(daysOld)
                    .viewsPerDay(viewsPerDay)
                    .growthRate(growthRate)
                    .build();
            })
            .sorted(Comparator.comparing(CompetitorAnalysis.GrowthPattern::getViewsPerDay).reversed())
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * 업로드 후 경과 일수 계산
     */
    private int calculateDaysOld(String publishedAt) {
        try {
            ZonedDateTime published = ZonedDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
            ZonedDateTime now = ZonedDateTime.now();
            long days = Duration.between(published, now).toDays();
            return (int) Math.max(1, days);  // 최소 1일
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", publishedAt);
            return 1;
        }
    }
    
    /**
     * 성장률 카테고리화
     */
    private String categorizeGrowthRate(long viewsPerDay) {
        if (viewsPerDay >= 10000) return "매우 빠름";
        if (viewsPerDay >= 5000) return "빠름";
        if (viewsPerDay >= 1000) return "보통";
        if (viewsPerDay >= 100) return "느림";
        return "매우 느림";
    }
    
    /**
     * 참여율 계산
     */
    private double calculateEngagementRate(YouTubeVideo video) {
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        long likes = video.getLikeCount() != null ? video.getLikeCount() : 0;
        long comments = video.getCommentCount() != null ? video.getCommentCount() : 0;
        
        if (views == 0) return 0;
        
        return ((double) (likes + comments) / views) * 100;
    }
    
    /**
     * 중앙값 계산
     */
    private long calculateMedian(List<Long> values) {
        if (values.isEmpty()) return 0;
        
        int size = values.size();
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2;
        } else {
            return values.get(size / 2);
        }
    }
    
    /**
     * 중앙값 계산 (Double)
     */
    private double calculateMedianDouble(List<Double> values) {
        if (values.isEmpty()) return 0;
        
        int size = values.size();
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2;
        } else {
            return values.get(size / 2);
        }
    }
}
