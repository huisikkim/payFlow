package com.example.payflow.youtube.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * YouTube 영상 종합 분석 리포트
 */
@Getter
@Builder
public class VideoAnalysisReport {
    
    // 기본 정보
    private String videoId;
    private String videoTitle;
    private String channel;
    private String channelId;
    private Long channelSubscribers;
    private String thumbnailUrl;
    private String publishedAt;
    private String categoryName;
    
    // 현재 통계
    private Long currentViews;
    private Long currentLikes;
    private Long currentComments;
    private Double engagementRate;
    
    // 예측 데이터
    private Long predictedViews;
    private Long predictedGrowth;
    private Double dailyGrowthRate;
    
    // 수익 예측
    private Long minRevenue;
    private Long maxRevenue;
    private Long avgRevenue;
    private Long predictedRevenue;  // 예측 조회수 기반 수익
    private Integer revenuePotentialScore;
    
    // 경쟁 분석
    private Integer competitionScore;
    private String competitionLevel;
    private Integer recentCompetitors;
    private Long avgCompetitorViews;
    private String competitionRecommendation;
    
    // 태그 & 키워드
    private List<String> tags;
    private List<String> extractedKeywords;
    
    // 종합 점수
    private Integer overallScore;  // 0-100
    private String overallGrade;   // S, A, B, C, D
    
    // 추천 사항
    private List<String> recommendations;
    private List<String> recommendedTitles;
    
    // 채널 연락처
    private String channelEmail;
    private String channelInstagram;
    private String channelTwitter;
    private String channelWebsite;
}
