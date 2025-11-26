package com.example.payflow.matching.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributorComparison {
    
    // 기본 정보
    private String distributorId;
    private String distributorName;
    private String phoneNumber;
    private String email;
    
    // 매칭 점수
    private BigDecimal totalScore;
    private BigDecimal regionScore;
    private BigDecimal productScore;
    private BigDecimal deliveryScore;
    private BigDecimal certificationScore;
    
    // 가격 정보
    private Integer minOrderAmount;
    private PriceLevel priceLevel;  // 가격대 (저렴/보통/비쌈)
    private String priceNote;  // 가격 관련 메모
    
    // 배송 정보
    private Boolean deliveryAvailable;
    private String deliveryInfo;
    private DeliverySpeed deliverySpeed;  // 배송 속도 (당일/익일/2-3일)
    private Integer deliveryFee;  // 배송비
    private String deliveryRegions;  // 배송 가능 지역
    
    // 서비스 정보
    private String serviceRegions;
    private String supplyProducts;
    private String certifications;
    private Integer certificationCount;  // 인증 개수
    private String operatingHours;
    
    // 품질 지표
    private QualityRating qualityRating;  // 품질 등급 (상/중/하)
    private BigDecimal reliabilityScore;  // 신뢰도 점수 (0-100)
    
    // 추가 정보
    private String description;
    private List<String> strengths;  // 강점 리스트
    private List<String> weaknesses;  // 약점 리스트
    
    // 비교 순위
    private Integer rank;  // 종합 순위
    private ComparisonCategory bestCategory;  // 가장 우수한 카테고리
    
    public enum PriceLevel {
        LOW("저렴"),
        MEDIUM("보통"),
        HIGH("비쌈");
        
        private final String description;
        
        PriceLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum DeliverySpeed {
        SAME_DAY("당일 배송"),
        NEXT_DAY("익일 배송"),
        TWO_TO_THREE_DAYS("2-3일 배송"),
        OVER_THREE_DAYS("3일 이상");
        
        private final String description;
        
        DeliverySpeed(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum QualityRating {
        EXCELLENT("최상"),
        GOOD("상"),
        AVERAGE("중"),
        BELOW_AVERAGE("하");
        
        private final String description;
        
        QualityRating(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum ComparisonCategory {
        PRICE("가격"),
        DELIVERY("배송"),
        QUALITY("품질"),
        SERVICE("서비스"),
        CERTIFICATION("인증");
        
        private final String description;
        
        ComparisonCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
