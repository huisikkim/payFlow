package com.example.payflow.sourcing.dto;

import com.example.payflow.sourcing.domain.KeywordAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordAnalysisResponse {
    
    private Long id;
    private String keyword;
    private String platform;
    private Long searchVolume;
    private Integer competitionScore;
    private Integer productCount;
    private Double averagePrice;
    private Double profitabilityScore;
    private String marketType;
    private String recommendation;
    private LocalDateTime analyzedAt;
    
    public static KeywordAnalysisResponse from(KeywordAnalysis analysis) {
        String recommendation = generateRecommendation(analysis);
        
        return KeywordAnalysisResponse.builder()
                .id(analysis.getId())
                .keyword(analysis.getKeyword())
                .platform(analysis.getPlatform())
                .searchVolume(analysis.getSearchVolume())
                .competitionScore(analysis.getCompetitionScore())
                .productCount(analysis.getProductCount())
                .averagePrice(analysis.getAveragePrice())
                .profitabilityScore(analysis.getProfitabilityScore())
                .marketType(analysis.getMarketType())
                .recommendation(recommendation)
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
    }
    
    private static String generateRecommendation(KeywordAnalysis analysis) {
        if ("BLUE_OCEAN".equals(analysis.getMarketType())) {
            return "블루오션 키워드입니다! 진입 추천도가 높습니다.";
        } else if ("RED_OCEAN".equals(analysis.getMarketType())) {
            return "레드오션 키워드입니다. 경쟁이 치열하니 신중한 진입이 필요합니다.";
        } else {
            return "일반적인 시장입니다. 차별화 전략이 필요합니다.";
        }
    }
}
