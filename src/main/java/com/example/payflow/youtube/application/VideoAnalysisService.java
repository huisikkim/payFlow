package com.example.payflow.youtube.application;

import com.example.payflow.youtube.domain.*;
import com.example.payflow.youtube.infrastructure.YouTubeApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * YouTube 영상 종합 분석 서비스
 * URL 입력 → 수익/조회수/경쟁 분석 자동 리포트 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoAnalysisService {
    
    private final YouTubeApiClient youTubeApiClient;
    private final YouTubeService youTubeService;
    private final RevenueEstimator revenueEstimator;
    private final ViewPredictionModel viewPredictionModel;
    private final CompetitionAnalyzer competitionAnalyzer;
    
    // 새로운 분석기들
    private final TitleAnalyzer titleAnalyzer;
    private final SeoAnalyzer seoAnalyzer;
    private final CompetitorAnalyzer competitorAnalyzer;
    private final CtrAnalyzer ctrAnalyzer;
    private final QualityScoreCalculator qualityScoreCalculator;
    
    /**
     * YouTube URL에서 videoId 추출
     */
    public String extractVideoId(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL이 비어있습니다.");
        }
        
        // 이미 videoId만 입력한 경우
        if (url.matches("^[a-zA-Z0-9_-]{11}$")) {
            return url;
        }
        
        // youtube.com/watch?v=VIDEO_ID
        Pattern pattern1 = Pattern.compile("(?:youtube\\.com/watch\\?v=)([a-zA-Z0-9_-]{11})");
        Matcher matcher1 = pattern1.matcher(url);
        if (matcher1.find()) {
            return matcher1.group(1);
        }
        
        // youtu.be/VIDEO_ID
        Pattern pattern2 = Pattern.compile("(?:youtu\\.be/)([a-zA-Z0-9_-]{11})");
        Matcher matcher2 = pattern2.matcher(url);
        if (matcher2.find()) {
            return matcher2.group(1);
        }
        
        // youtube.com/embed/VIDEO_ID
        Pattern pattern3 = Pattern.compile("(?:youtube\\.com/embed/)([a-zA-Z0-9_-]{11})");
        Matcher matcher3 = pattern3.matcher(url);
        if (matcher3.find()) {
            return matcher3.group(1);
        }
        
        throw new IllegalArgumentException("유효하지 않은 YouTube URL입니다: " + url);
    }
    
    /**
     * 영상 종합 분석 리포트 생성
     */
    public VideoAnalysisReport analyzeVideo(String videoId) {
        log.info("영상 분석 시작 - videoId: {}", videoId);
        
        // 1. 영상 상세 정보 조회
        List<YouTubeVideo> videos = youTubeApiClient.getVideoDetails(Arrays.asList(videoId));
        if (videos.isEmpty()) {
            throw new IllegalArgumentException("영상을 찾을 수 없습니다: " + videoId);
        }
        
        YouTubeVideo video = videos.get(0);
        
        // 2. 채널 정보 추가
        enrichWithChannelInfo(video);
        
        // 3. 키워드 추출 (제목에서)
        List<String> keywords = extractKeywords(video.getTitle());
        
        // 4. 경쟁 영상 검색 (첫 번째 키워드 사용)
        List<YouTubeVideo> competitors = new ArrayList<>();
        CompetitionAnalyzer.CompetitionScore competitionScore = null;
        
        if (!keywords.isEmpty()) {
            try {
                String mainKeyword = keywords.get(0);
                competitors = youTubeApiClient.searchVideos(mainKeyword, 50);
                competitionScore = competitionAnalyzer.analyzeCompetition(video, competitors);
            } catch (Exception e) {
                log.warn("경쟁 분석 실패", e);
            }
        }
        
        // 5. 조회수 예측
        long predictedViews = viewPredictionModel.predictFutureViews(video, 30);
        double growthRate = viewPredictionModel.calculateGrowthRate(video);
        
        // 6. 수익 예측 (현재 + 미래)
        RevenueEstimate currentRevenue = revenueEstimator.estimateRevenue(video);
        
        // 미래 수익 예측 (예측 조회수 기반)
        YouTubeVideo futureVideo = video.toBuilder()
                .viewCount(predictedViews)
                .build();
        RevenueEstimate futureRevenue = revenueEstimator.estimateRevenue(futureVideo);
        
        // 7. 참여율 계산
        double engagementRate = calculateEngagementRate(video);
        
        // 8. 종합 점수 계산
        int overallScore = calculateOverallScore(video, competitionScore, currentRevenue);
        String overallGrade = getGrade(overallScore);
        
        // 9. 추천 사항 생성
        List<String> recommendations = generateRecommendations(
                video, 
                competitionScore, 
                currentRevenue, 
                overallScore
        );
        
        // 10. 제목 추천 (경쟁 영상 기반)
        List<String> recommendedTitles = generateTitleRecommendations(video, competitors);
        
        // ===== 새로운 분석 기능 =====
        
        // 11. 제목 분석
        TitleAnalysis titleAnalysis = titleAnalyzer.analyze(video.getTitle(), competitors);
        
        // 12. SEO 분석
        SeoAnalysis seoAnalysis = seoAnalyzer.analyze(video, competitors);
        
        // 13. 경쟁 영상 상세 분석
        CompetitorAnalysis competitorAnalysis = competitorAnalyzer.analyzeDetailed(video, competitors);
        
        // 14. CTR 추정
        CtrEstimate ctrEstimate = ctrAnalyzer.estimate(video, titleAnalysis, seoAnalysis);
        
        // 15. 품질 세부 점수
        QualityScore qualityScore = qualityScoreCalculator.calculate(titleAnalysis, seoAnalysis, video);
        
        // 16. 리포트 생성
        return VideoAnalysisReport.builder()
                // 기본 정보
                .videoId(video.getVideoId())
                .videoTitle(video.getTitle())
                .channel(video.getChannelTitle())
                .channelId(video.getChannelId())
                .channelSubscribers(video.getChannelSubscriberCount())
                .thumbnailUrl(video.getThumbnailUrl())
                .publishedAt(video.getPublishedAt())
                .categoryName(currentRevenue.getCategoryName())
                
                // 현재 통계
                .currentViews(video.getViewCount())
                .currentLikes(video.getLikeCount())
                .currentComments(video.getCommentCount())
                .engagementRate(engagementRate)
                
                // 예측 데이터
                .predictedViews(predictedViews)
                .predictedGrowth(predictedViews - video.getViewCount())
                .dailyGrowthRate(growthRate)
                
                // 수익 예측
                .minRevenue(currentRevenue.getMinRevenue())
                .maxRevenue(currentRevenue.getMaxRevenue())
                .avgRevenue(currentRevenue.getAvgRevenue())
                .predictedRevenue(futureRevenue.getAvgRevenue())
                .revenuePotentialScore(currentRevenue.getPotentialScore())
                
                // 경쟁 분석 (기존)
                .competitionScore(competitionScore != null ? competitionScore.getScore() : 50)
                .competitionLevel(competitionScore != null ? competitionScore.getLevel() : "알 수 없음")
                .recentCompetitors(competitionScore != null ? competitionScore.getRecentCompetitors() : 0)
                .avgCompetitorViews(competitionScore != null ? competitionScore.getAvgViews() : 0L)
                .competitionRecommendation(competitionScore != null ? competitionScore.getRecommendation() : "")
                
                // 태그 & 키워드
                .tags(new ArrayList<>())  // YouTube API v3에서는 tags가 제한됨
                .extractedKeywords(keywords)
                
                // 종합 점수
                .overallScore(overallScore)
                .overallGrade(overallGrade)
                
                // 추천 사항
                .recommendations(recommendations)
                .recommendedTitles(recommendedTitles)
                
                // 채널 연락처
                .channelEmail(video.getChannelEmail())
                .channelInstagram(video.getChannelInstagram())
                .channelTwitter(video.getChannelTwitter())
                .channelWebsite(video.getChannelWebsite())
                
                // ===== 새로운 분석 결과 =====
                .titleAnalysis(titleAnalysis)
                .seoAnalysis(seoAnalysis)
                .competitorAnalysis(competitorAnalysis)
                .ctrEstimate(ctrEstimate)
                .qualityScore(qualityScore)
                
                .build();
    }
    
    /**
     * 채널 정보 추가
     */
    private void enrichWithChannelInfo(YouTubeVideo video) {
        if (video.getChannelId() != null) {
            var channelInfos = youTubeApiClient.getChannelInfos(Arrays.asList(video.getChannelId()));
            YouTubeApiClient.ChannelInfo info = channelInfos.get(video.getChannelId());
            if (info != null) {
                video.setChannelSubscriberCount(info.subscriberCount);
                video.setChannelDescription(info.description);
                video.setChannelEmail(info.email);
                video.setChannelInstagram(info.instagram);
                video.setChannelTwitter(info.twitter);
                video.setChannelWebsite(info.website);
            }
        }
    }
    
    /**
     * 제목에서 키워드 추출
     */
    private List<String> extractKeywords(String title) {
        if (title == null) return new ArrayList<>();
        
        // 간단한 키워드 추출 (공백 기준 분리 + 불용어 제거)
        String[] words = title.split("\\s+");
        List<String> stopWords = Arrays.asList("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", 
                "of", "with", "by", "from", "up", "about", "into", "through", "during", "before", "after",
                "이", "그", "저", "것", "수", "등", "및", "를", "을", "가", "이", "에", "의", "와", "과");
        
        return Arrays.stream(words)
                .map(String::toLowerCase)
                .filter(word -> word.length() > 2)
                .filter(word -> !stopWords.contains(word))
                .limit(5)
                .collect(Collectors.toList());
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
     * 종합 점수 계산 (0-100)
     */
    private int calculateOverallScore(
            YouTubeVideo video, 
            CompetitionAnalyzer.CompetitionScore competitionScore,
            RevenueEstimate revenueEstimate) {
        
        int score = 0;
        
        // 1. 수익 잠재력 (40점)
        score += (revenueEstimate.getPotentialScore() * 0.4);
        
        // 2. 경쟁 우위 (30점) - 경쟁이 낮을수록 좋음
        if (competitionScore != null) {
            int competitionBonus = 100 - competitionScore.getScore();
            score += (competitionBonus * 0.3);
        } else {
            score += 15; // 기본 점수
        }
        
        // 3. 현재 성과 (30점)
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        int performanceScore = 0;
        if (views > 1000000) performanceScore = 30;
        else if (views > 500000) performanceScore = 25;
        else if (views > 100000) performanceScore = 20;
        else if (views > 50000) performanceScore = 15;
        else if (views > 10000) performanceScore = 10;
        else performanceScore = 5;
        score += performanceScore;
        
        return Math.min(100, score);
    }
    
    /**
     * 등급 계산
     */
    private String getGrade(int score) {
        if (score >= 90) return "S";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }
    
    /**
     * 추천 사항 생성
     */
    private List<String> generateRecommendations(
            YouTubeVideo video,
            CompetitionAnalyzer.CompetitionScore competitionScore,
            RevenueEstimate revenueEstimate,
            int overallScore) {
        
        List<String> recommendations = new ArrayList<>();
        
        // 1. 종합 평가
        if (overallScore >= 80) {
            recommendations.add("✅ 매우 우수한 영상입니다! 이 스타일을 유지하세요.");
        } else if (overallScore >= 60) {
            recommendations.add("👍 좋은 성과를 내고 있습니다. 몇 가지 개선으로 더 성장할 수 있습니다.");
        } else {
            recommendations.add("💡 개선의 여지가 많습니다. 아래 추천 사항을 참고하세요.");
        }
        
        // 2. 참여율 기반 추천
        double engagementRate = calculateEngagementRate(video);
        if (engagementRate < 1.0) {
            recommendations.add("📢 참여율이 낮습니다. 시청자와의 소통을 늘리고, CTA(Call-to-Action)를 추가하세요.");
        } else if (engagementRate > 5.0) {
            recommendations.add("🔥 참여율이 매우 높습니다! 알고리즘이 선호하는 콘텐츠입니다.");
        }
        
        // 3. 경쟁도 기반 추천
        if (competitionScore != null) {
            if (competitionScore.getScore() >= 70) {
                recommendations.add("⚠️ 경쟁이 치열합니다. 틈새 키워드나 차별화된 콘텐츠를 고려하세요.");
            } else if (competitionScore.getScore() <= 30) {
                recommendations.add("🎯 경쟁이 낮은 블루오션입니다. 적극적으로 콘텐츠를 생산하세요!");
            }
        }
        
        // 4. 수익 잠재력 기반 추천
        if (revenueEstimate.getPotentialScore() >= 70) {
            recommendations.add("💰 수익 잠재력이 높습니다. 광고 최적화와 스폰서십을 고려하세요.");
        }
        
        // 5. 채널 성장 추천
        Long subscribers = video.getChannelSubscriberCount();
        Long views = video.getViewCount();
        if (subscribers != null && views != null && views > subscribers * 2) {
            recommendations.add("📈 바이럴 가능성이 높습니다. 구독 유도를 강화하세요.");
        }
        
        return recommendations;
    }
    
    /**
     * 제목 추천 생성 (경쟁 영상 분석 기반)
     */
    private List<String> generateTitleRecommendations(YouTubeVideo video, List<YouTubeVideo> competitors) {
        List<String> recommendations = new ArrayList<>();
        
        if (competitors.isEmpty()) {
            return recommendations;
        }
        
        // 상위 조회수 영상의 제목 패턴 분석
        List<YouTubeVideo> topVideos = competitors.stream()
                .sorted((v1, v2) -> Long.compare(
                        v2.getViewCount() != null ? v2.getViewCount() : 0,
                        v1.getViewCount() != null ? v1.getViewCount() : 0
                ))
                .limit(3)
                .collect(Collectors.toList());
        
        // 간단한 제목 추천 (실제로는 OpenAI API 사용 가능)
        String currentTitle = video.getTitle();
        
        // 패턴 1: 숫자 추가
        if (!currentTitle.matches(".*\\d+.*")) {
            recommendations.add(currentTitle + " - 2025년 최신 가이드");
        }
        
        // 패턴 2: 감정 단어 추가
        if (!currentTitle.toLowerCase().contains("완벽") && !currentTitle.toLowerCase().contains("최고")) {
            recommendations.add("완벽한 " + currentTitle);
        }
        
        // 패턴 3: 시간 강조
        if (!currentTitle.contains("분") && !currentTitle.contains("min")) {
            recommendations.add(currentTitle + " (10분 완성)");
        }
        
        return recommendations.stream().limit(3).collect(Collectors.toList());
    }
}
