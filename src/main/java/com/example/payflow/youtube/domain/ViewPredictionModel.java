package com.example.payflow.youtube.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * YouTube 조회수 예측 모델
 * 영상의 현재 성과를 기반으로 미래 조회수를 예측
 */
@Slf4j
@Component
public class ViewPredictionModel {
    
    /**
     * 미래 조회수 예측 (30일 후)
     */
    public long predictFutureViews(YouTubeVideo video) {
        return predictFutureViews(video, 30);
    }
    
    /**
     * 미래 조회수 예측
     * @param video 분석할 영상
     * @param daysAhead 예측할 일수
     * @return 예상 총 조회수
     */
    public long predictFutureViews(YouTubeVideo video, int daysAhead) {
        long currentViews = video.getViewCount() != null ? video.getViewCount() : 0;
        
        if (currentViews == 0) {
            return 0;
        }
        
        int daysSincePublish = calculateDaysSincePublish(video.getPublishedAt());
        
        if (daysSincePublish <= 0) {
            daysSincePublish = 1; // 최소 1일
        }
        
        // 1. 일일 평균 조회수
        double dailyAvgViews = (double) currentViews / daysSincePublish;
        
        // 2. 성장 곡선 (초기 급증 → 점진적 감소)
        // 영상은 초기에 조회수가 급증하고 시간이 지날수록 감소
        double decayFactor = Math.exp(-0.03 * daysSincePublish);
        
        // 3. 채널 파워 (구독자 대비 조회수 비율)
        double channelPower = calculateChannelPower(video);
        
        // 4. 참여율 보너스 (높은 참여율 = 알고리즘 추천 증가)
        double engagementBonus = calculateEngagementBonus(video);
        
        // 5. 예측 조회수 증가량
        long predictedGrowth = (long) (dailyAvgViews * daysAhead * decayFactor * channelPower * engagementBonus);
        
        // 6. 최종 예측 조회수
        long predictedViews = currentViews + predictedGrowth;
        
        log.debug("조회수 예측 - 현재: {}, 예측: {}, 증가: {}, 일평균: {}, 감쇠: {}, 채널파워: {}, 참여보너스: {}", 
                currentViews, predictedViews, predictedGrowth, dailyAvgViews, decayFactor, channelPower, engagementBonus);
        
        return predictedViews;
    }
    
    /**
     * 업로드 후 경과 일수 계산
     */
    private int calculateDaysSincePublish(String publishedAt) {
        if (publishedAt == null) {
            return 1;
        }
        
        try {
            ZonedDateTime publishDate = ZonedDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
            ZonedDateTime now = ZonedDateTime.now();
            long days = Duration.between(publishDate, now).toDays();
            return (int) Math.max(1, days);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", publishedAt);
            return 1;
        }
    }
    
    /**
     * 채널 파워 계산 (구독자 대비 조회수)
     * 높을수록 채널의 영향력이 크다는 의미
     */
    private double calculateChannelPower(YouTubeVideo video) {
        Long subscribers = video.getChannelSubscriberCount();
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        
        if (subscribers == null || subscribers == 0) {
            // 구독자 정보 없으면 조회수 기반으로만 계산
            // 조회수가 높으면 채널 파워도 높다고 가정
            if (views > 1000000) return 1.5;  // 100만 이상
            if (views > 100000) return 1.3;   // 10만 이상
            if (views > 10000) return 1.1;    // 1만 이상
            return 1.0;
        }
        
        // 조회수 / 구독자 비율
        double ratio = (double) views / subscribers;
        
        // 비율이 높을수록 바이럴 가능성 높음
        if (ratio > 5.0) return 2.0;   // 구독자의 5배 이상 조회 = 매우 강력
        if (ratio > 2.0) return 1.5;   // 구독자의 2배 이상 조회 = 강력
        if (ratio > 1.0) return 1.3;   // 구독자보다 많은 조회 = 좋음
        if (ratio > 0.5) return 1.1;   // 구독자의 절반 이상 조회 = 보통
        return 1.0;                     // 그 외 = 기본
    }
    
    /**
     * 참여율 보너스 계산
     * 높은 참여율 = YouTube 알고리즘이 더 많이 추천
     */
    private double calculateEngagementBonus(YouTubeVideo video) {
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        long likes = video.getLikeCount() != null ? video.getLikeCount() : 0;
        long comments = video.getCommentCount() != null ? video.getCommentCount() : 0;
        
        if (views == 0) {
            return 1.0;
        }
        
        // 참여율 = (좋아요 + 댓글) / 조회수
        double engagementRate = ((double) (likes + comments) / views) * 100;
        
        // 참여율이 높을수록 보너스
        if (engagementRate > 10) return 1.5;   // 10% 이상 = 매우 높음
        if (engagementRate > 5) return 1.3;    // 5% 이상 = 높음
        if (engagementRate > 2) return 1.2;    // 2% 이상 = 좋음
        if (engagementRate > 1) return 1.1;    // 1% 이상 = 보통
        return 1.0;                             // 그 외 = 기본
    }
    
    /**
     * 성장 속도 계산 (일일 조회수 증가율)
     */
    public double calculateGrowthRate(YouTubeVideo video) {
        long currentViews = video.getViewCount() != null ? video.getViewCount() : 0;
        int daysSincePublish = calculateDaysSincePublish(video.getPublishedAt());
        
        if (daysSincePublish <= 0 || currentViews == 0) {
            return 0;
        }
        
        return (double) currentViews / daysSincePublish;
    }
}
