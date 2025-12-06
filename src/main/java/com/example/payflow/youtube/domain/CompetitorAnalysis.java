package com.example.payflow.youtube.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 경쟁 영상 분석 결과 (강화 버전)
 */
@Getter
@Builder
public class CompetitorAnalysis {
    private Integer totalCompetitors;
    private Integer top20Count;
    
    // 조회수 통계
    private Long viewsMedian;
    private Long viewsAverage;
    private Long viewsMin;
    private Long viewsMax;
    
    // 참여도 통계
    private Double engagementMedian;
    private Double engagementAverage;
    
    // 업로드 시간 대비 성장 패턴
    private List<GrowthPattern> growthPatterns;
    
    // 상위 경쟁 영상
    private List<CompetitorVideo> topCompetitors;
    
    @Getter
    @Builder
    public static class GrowthPattern {
        private String videoId;
        private String title;
        private Long views;
        private Integer daysOld;
        private Long viewsPerDay;
        private String growthRate;  // "빠름", "보통", "느림"
    }
    
    @Getter
    @Builder
    public static class CompetitorVideo {
        private String videoId;
        private String title;
        private String channel;
        private Long views;
        private Long likes;
        private Integer daysOld;
        private Double engagementRate;
    }
}
