package com.example.payflow.sourcing.service;

import com.example.payflow.sourcing.domain.KeywordAnalysis;
import com.example.payflow.sourcing.domain.KeywordAnalysisRepository;
import com.example.payflow.sourcing.dto.KeywordAnalysisRequest;
import com.example.payflow.sourcing.dto.KeywordAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordAnalysisService {
    
    private final KeywordCrawlerService crawlerService;
    private final KeywordAnalysisRepository repository;
    
    /**
     * 키워드 분석 실행
     */
    @Transactional
    public List<KeywordAnalysisResponse> analyzeKeyword(KeywordAnalysisRequest request) {
        List<KeywordAnalysisResponse> results = new ArrayList<>();
        String keyword = request.getKeyword();
        String platform = request.getPlatform();
        
        log.info("키워드 분석 시작 - 키워드: {}, 플랫폼: {}", keyword, platform);
        
        if ("ALL".equals(platform) || "NAVER".equals(platform)) {
            KeywordAnalysis naverAnalysis = analyzeForPlatform(keyword, "NAVER");
            results.add(KeywordAnalysisResponse.from(naverAnalysis));
        }
        
        if ("ALL".equals(platform) || "COUPANG".equals(platform)) {
            KeywordAnalysis coupangAnalysis = analyzeForPlatform(keyword, "COUPANG");
            results.add(KeywordAnalysisResponse.from(coupangAnalysis));
        }
        
        log.info("키워드 분석 완료 - 결과 수: {}", results.size());
        return results;
    }
    
    /**
     * 특정 플랫폼에 대한 키워드 분석
     */
    private KeywordAnalysis analyzeForPlatform(String keyword, String platform) {
        // 데이터 수집
        Map<String, Object> crawlData = "NAVER".equals(platform) 
                ? crawlerService.crawlNaverShopping(keyword)
                : crawlerService.crawlCoupang(keyword);
        
        // 분석 수행
        Long searchVolume = (Long) crawlData.get("searchVolume");
        Integer productCount = (Integer) crawlData.get("productCount");
        Double averagePrice = (Double) crawlData.get("averagePrice");
        
        // 경쟁도 계산 (상품 수 기반, 0-100 스케일)
        int competitionScore = calculateCompetitionScore(productCount);
        
        // 수익성 점수 계산 (검색량 / 경쟁도)
        double profitabilityScore = calculateProfitabilityScore(searchVolume, competitionScore);
        
        // 시장 타입 결정
        String marketType = determineMarketType(profitabilityScore, competitionScore);
        
        // 엔티티 생성 및 저장
        KeywordAnalysis analysis = KeywordAnalysis.builder()
                .keyword(keyword)
                .platform(platform)
                .searchVolume(searchVolume)
                .competitionScore(competitionScore)
                .productCount(productCount)
                .averagePrice(averagePrice)
                .profitabilityScore(profitabilityScore)
                .marketType(marketType)
                .build();
        
        return repository.save(analysis);
    }
    
    /**
     * 경쟁도 계산 (0-100)
     */
    private int calculateCompetitionScore(int productCount) {
        // 상품 수를 0-100 스케일로 변환
        if (productCount < 100) return 10;
        if (productCount < 500) return 30;
        if (productCount < 1000) return 50;
        if (productCount < 2000) return 70;
        return 90;
    }
    
    /**
     * 수익성 점수 계산
     */
    private double calculateProfitabilityScore(long searchVolume, int competitionScore) {
        if (competitionScore == 0) return 0.0;
        return (double) searchVolume / competitionScore;
    }
    
    /**
     * 시장 타입 결정
     */
    private String determineMarketType(double profitabilityScore, int competitionScore) {
        // 블루오션: 높은 수익성 점수 + 낮은 경쟁도
        if (profitabilityScore > 500 && competitionScore < 50) {
            return "BLUE_OCEAN";
        }
        // 레드오션: 낮은 수익성 점수 + 높은 경쟁도
        else if (profitabilityScore < 200 && competitionScore > 70) {
            return "RED_OCEAN";
        }
        // 일반 시장
        return "NORMAL";
    }
    
    /**
     * 블루오션 키워드 조회
     */
    @Transactional(readOnly = true)
    public List<KeywordAnalysisResponse> getBlueOceanKeywords() {
        return repository.findByMarketTypeOrderByProfitabilityScoreDesc("BLUE_OCEAN")
                .stream()
                .map(KeywordAnalysisResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 최근 분석 결과 조회
     */
    @Transactional(readOnly = true)
    public List<KeywordAnalysisResponse> getRecentAnalysis(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return repository.findRecentAnalysis(since)
                .stream()
                .map(KeywordAnalysisResponse::from)
                .collect(Collectors.toList());
    }
}
