package com.example.payflow.youtube.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * YouTube 수익 예측 계산기
 */
@Slf4j
@Component
public class RevenueEstimator {
    
    // 카테고리별 평균 CPM (1000회 조회당 수익, 단위: 원)
    // 실제 데이터 기반 추정치
    private static final Map<String, CpmRange> CATEGORY_CPM = Map.ofEntries(
        Map.entry("1", new CpmRange(3000, 8000, "영화/애니메이션")),      // Film & Animation
        Map.entry("2", new CpmRange(2000, 5000, "자동차/차량")),          // Autos & Vehicles
        Map.entry("10", new CpmRange(4000, 10000, "음악")),              // Music
        Map.entry("15", new CpmRange(2000, 6000, "반려동물/동물")),       // Pets & Animals
        Map.entry("17", new CpmRange(3000, 8000, "스포츠")),             // Sports
        Map.entry("19", new CpmRange(2000, 5000, "여행/이벤트")),         // Travel & Events
        Map.entry("20", new CpmRange(3000, 7000, "게임")),               // Gaming
        Map.entry("22", new CpmRange(4000, 12000, "브이로그")),          // People & Blogs
        Map.entry("23", new CpmRange(3000, 8000, "코미디")),             // Comedy
        Map.entry("24", new CpmRange(4000, 10000, "엔터테인먼트")),       // Entertainment
        Map.entry("25", new CpmRange(3000, 8000, "뉴스/정치")),          // News & Politics
        Map.entry("26", new CpmRange(5000, 15000, "하우투/스타일")),      // Howto & Style
        Map.entry("27", new CpmRange(6000, 20000, "교육")),              // Education
        Map.entry("28", new CpmRange(8000, 25000, "과학/기술")),         // Science & Technology
        Map.entry("29", new CpmRange(3000, 8000, "비영리/사회운동"))      // Nonprofits & Activism
    );
    
    private static final CpmRange DEFAULT_CPM = new CpmRange(3000, 8000, "일반");
    
    /**
     * 예상 광고 수익 계산
     */
    public RevenueEstimate estimateRevenue(YouTubeVideo video) {
        long viewCount = video.getViewCount() != null ? video.getViewCount() : 0;
        String categoryId = video.getCategoryId();
        
        CpmRange cpmRange = CATEGORY_CPM.getOrDefault(categoryId, DEFAULT_CPM);
        
        // 수익 계산 (조회수 / 1000 * CPM)
        long minRevenue = (viewCount / 1000) * cpmRange.getMin();
        long maxRevenue = (viewCount / 1000) * cpmRange.getMax();
        long avgRevenue = (minRevenue + maxRevenue) / 2;
        
        // 수익 잠재력 점수 계산 (0-100)
        int potentialScore = calculateRevenuePotential(video, cpmRange);
        
        return RevenueEstimate.builder()
                .minRevenue(minRevenue)
                .maxRevenue(maxRevenue)
                .avgRevenue(avgRevenue)
                .cpmMin(cpmRange.getMin())
                .cpmMax(cpmRange.getMax())
                .categoryName(cpmRange.getCategoryName())
                .potentialScore(potentialScore)
                .build();
    }
    
    /**
     * 수익 잠재력 점수 계산
     * - 참여율 (40%): 높을수록 알고리즘 추천 가능성 높음
     * - 바이럴 지수 (30%): 비구독자 유입이 많을수록 좋음
     * - CPM 등급 (30%): 카테고리별 광고 단가
     */
    private int calculateRevenuePotential(YouTubeVideo video, CpmRange cpmRange) {
        // 1. 참여율 점수 (0-100)
        double engagementScore = calculateEngagementScore(video);
        
        // 2. 바이럴 점수 (0-100)
        double viralScore = calculateViralScore(video);
        
        // 3. CPM 점수 (0-100)
        double cpmScore = ((double) cpmRange.getMax() / 25000) * 100; // 25000원이 최고 CPM
        cpmScore = Math.min(100, cpmScore);
        
        // 가중 평균
        double totalScore = (engagementScore * 0.4) + (viralScore * 0.3) + (cpmScore * 0.3);
        
        return (int) Math.round(totalScore);
    }
    
    private double calculateEngagementScore(YouTubeVideo video) {
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        long likes = video.getLikeCount() != null ? video.getLikeCount() : 0;
        long comments = video.getCommentCount() != null ? video.getCommentCount() : 0;
        
        if (views == 0) return 0;
        
        double engagementRate = ((double) (likes + comments) / views) * 100;
        // 10% 참여율 = 100점
        return Math.min(100, (engagementRate / 10) * 100);
    }
    
    private double calculateViralScore(YouTubeVideo video) {
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        Long subscribers = video.getChannelSubscriberCount();
        
        if (subscribers == null || subscribers == 0) {
            // 구독자 정보 없으면 조회수 기반으로만 계산
            // 100만 조회 = 100점
            return Math.min(100, (Math.log10(views + 1) / 6) * 100);
        }
        
        double viralIndex = ((double) views / subscribers) * 100;
        // 500% 이상 = 100점
        return Math.min(100, (viralIndex / 500) * 100);
    }
    
    /**
     * 월 수익 시뮬레이션
     * 이 스타일로 계속 영상을 만들면 예상되는 월 수익
     */
    public MonthlyRevenueSimulation simulateMonthlyRevenue(
            YouTubeVideo video, 
            int videosPerMonth) {
        
        RevenueEstimate singleVideoRevenue = estimateRevenue(video);
        
        // 월 수익 = 영상당 수익 × 월 영상 개수
        long monthlyMin = singleVideoRevenue.getMinRevenue() * videosPerMonth;
        long monthlyMax = singleVideoRevenue.getMaxRevenue() * videosPerMonth;
        long monthlyAvg = singleVideoRevenue.getAvgRevenue() * videosPerMonth;
        
        // 성장 가능성 (바이럴 잠재력 기반)
        String growthPotential = getGrowthPotential(singleVideoRevenue.getPotentialScore());
        
        return MonthlyRevenueSimulation.builder()
                .videosPerMonth(videosPerMonth)
                .monthlyMinRevenue(monthlyMin)
                .monthlyMaxRevenue(monthlyMax)
                .monthlyAvgRevenue(monthlyAvg)
                .growthPotential(growthPotential)
                .potentialScore(singleVideoRevenue.getPotentialScore())
                .build();
    }
    
    private String getGrowthPotential(int score) {
        if (score >= 80) return "매우 높음 🚀";
        if (score >= 60) return "높음 📈";
        if (score >= 40) return "보통 ➡️";
        if (score >= 20) return "낮음 📉";
        return "매우 낮음 ⚠️";
    }
    
    // Inner classes
    public static class CpmRange {
        private final int min;
        private final int max;
        private final String categoryName;
        
        public CpmRange(int min, int max, String categoryName) {
            this.min = min;
            this.max = max;
            this.categoryName = categoryName;
        }
        
        public int getMin() { return min; }
        public int getMax() { return max; }
        public String getCategoryName() { return categoryName; }
    }
}
