package com.example.payflow.matching.application;

import com.example.payflow.distributor.domain.Distributor;
import com.example.payflow.distributor.domain.DistributorRepository;
import com.example.payflow.matching.domain.DistributorComparison;
import com.example.payflow.matching.domain.MatchingScore;
import com.example.payflow.store.domain.Store;
import com.example.payflow.store.domain.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributorComparisonService {
    
    private final DistributorRepository distributorRepository;
    private final StoreRepository storeRepository;
    private final MatchingService matchingService;
    
    /**
     * 여러 유통업체 비교
     */
    @Transactional(readOnly = true)
    public List<DistributorComparison> compareDistributors(String storeId, List<String> distributorIds) {
        // 매장 정보 조회
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        // 유통업체 조회
        List<Distributor> distributors = distributorIds.stream()
                .map(id -> distributorRepository.findByDistributorId(id)
                        .orElseThrow(() -> new IllegalArgumentException("유통업체를 찾을 수 없습니다: " + id)))
                .collect(Collectors.toList());
        
        // 각 유통업체별 비교 정보 생성
        List<DistributorComparison> comparisons = distributors.stream()
                .map(distributor -> createComparison(store, distributor))
                .collect(Collectors.toList());
        
        // 순위 매기기
        assignRanks(comparisons);
        
        log.info("✅ 유통업체 비교 완료 - 매장: {}, 비교 대상: {}개", storeId, comparisons.size());
        
        return comparisons;
    }
    
    /**
     * 추천 유통업체 비교 (Top N)
     */
    @Transactional(readOnly = true)
    public List<DistributorComparison> compareTopDistributors(String storeId, int topN) {
        // 매장 정보 조회
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        // 추천 유통업체 조회
        List<MatchingScore> recommendations = matchingService.recommendDistributors(storeId, topN);
        
        // 유통업체 ID 추출
        List<String> distributorIds = recommendations.stream()
                .map(MatchingScore::getDistributorId)
                .collect(Collectors.toList());
        
        // 비교 정보 생성
        return compareDistributors(storeId, distributorIds);
    }
    
    /**
     * 카테고리별 최고 유통업체 찾기
     */
    @Transactional(readOnly = true)
    public Map<String, DistributorComparison> findBestByCategory(String storeId, List<String> distributorIds) {
        List<DistributorComparison> comparisons = compareDistributors(storeId, distributorIds);
        
        Map<String, DistributorComparison> bestByCategory = new HashMap<>();
        
        // 가격 최고
        comparisons.stream()
                .min(Comparator.comparing(c -> c.getMinOrderAmount() != null ? c.getMinOrderAmount() : Integer.MAX_VALUE))
                .ifPresent(c -> bestByCategory.put("PRICE", c));
        
        // 배송 최고
        comparisons.stream()
                .max(Comparator.comparing(DistributorComparison::getDeliveryScore))
                .ifPresent(c -> bestByCategory.put("DELIVERY", c));
        
        // 품질 최고
        comparisons.stream()
                .max(Comparator.comparing(DistributorComparison::getReliabilityScore))
                .ifPresent(c -> bestByCategory.put("QUALITY", c));
        
        // 인증 최고
        comparisons.stream()
                .max(Comparator.comparing(DistributorComparison::getCertificationScore))
                .ifPresent(c -> bestByCategory.put("CERTIFICATION", c));
        
        // 종합 최고
        comparisons.stream()
                .max(Comparator.comparing(DistributorComparison::getTotalScore))
                .ifPresent(c -> bestByCategory.put("OVERALL", c));
        
        return bestByCategory;
    }
    
    /**
     * 비교 정보 생성
     */
    private DistributorComparison createComparison(Store store, Distributor distributor) {
        // 매칭 점수 계산
        MatchingScore matchingScore = calculateMatchingScore(store, distributor);
        
        // 가격 레벨 계산
        DistributorComparison.PriceLevel priceLevel = calculatePriceLevel(distributor.getMinOrderAmount());
        
        // 배송 속도 계산
        DistributorComparison.DeliverySpeed deliverySpeed = calculateDeliverySpeed(distributor.getDeliveryInfo());
        
        // 품질 등급 계산
        DistributorComparison.QualityRating qualityRating = calculateQualityRating(distributor);
        
        // 신뢰도 점수 계산
        BigDecimal reliabilityScore = calculateReliabilityScore(distributor);
        
        // 강점/약점 분석
        List<String> strengths = analyzeStrengths(distributor, matchingScore);
        List<String> weaknesses = analyzeWeaknesses(distributor, matchingScore);
        
        // 최고 카테고리 찾기
        DistributorComparison.ComparisonCategory bestCategory = findBestCategory(matchingScore);
        
        // 인증 개수
        int certificationCount = distributor.getCertifications() != null 
                ? distributor.getCertifications().split(",").length 
                : 0;
        
        return DistributorComparison.builder()
                .distributorId(distributor.getDistributorId())
                .distributorName(distributor.getDistributorName())
                .phoneNumber(distributor.getPhoneNumber())
                .email(distributor.getEmail())
                .totalScore(matchingScore.getTotalScore())
                .regionScore(matchingScore.getRegionScore())
                .productScore(matchingScore.getProductScore())
                .deliveryScore(matchingScore.getDeliveryScore())
                .certificationScore(matchingScore.getCertificationScore())
                .minOrderAmount(distributor.getMinOrderAmount())
                .priceLevel(priceLevel)
                .priceNote(generatePriceNote(distributor.getMinOrderAmount()))
                .deliveryAvailable(distributor.getDeliveryAvailable())
                .deliveryInfo(distributor.getDeliveryInfo())
                .deliverySpeed(deliverySpeed)
                .deliveryFee(extractDeliveryFee(distributor.getDeliveryInfo()))
                .deliveryRegions(distributor.getServiceRegions())
                .serviceRegions(distributor.getServiceRegions())
                .supplyProducts(distributor.getSupplyProducts())
                .certifications(distributor.getCertifications())
                .certificationCount(certificationCount)
                .operatingHours(distributor.getOperatingHours())
                .qualityRating(qualityRating)
                .reliabilityScore(reliabilityScore)
                .description(distributor.getDescription())
                .strengths(strengths)
                .weaknesses(weaknesses)
                .bestCategory(bestCategory)
                .build();
    }
    
    /**
     * 매칭 점수 계산 (MatchingService 재사용)
     */
    private MatchingScore calculateMatchingScore(Store store, Distributor distributor) {
        // MatchingService의 로직을 재사용하기 위해 임시로 추천 조회
        List<MatchingScore> scores = matchingService.recommendDistributors(store.getStoreId(), 100);
        return scores.stream()
                .filter(s -> s.getDistributorId().equals(distributor.getDistributorId()))
                .findFirst()
                .orElse(MatchingScore.builder()
                        .distributorId(distributor.getDistributorId())
                        .distributorName(distributor.getDistributorName())
                        .totalScore(BigDecimal.ZERO)
                        .regionScore(BigDecimal.ZERO)
                        .productScore(BigDecimal.ZERO)
                        .deliveryScore(BigDecimal.ZERO)
                        .certificationScore(BigDecimal.ZERO)
                        .build());
    }
    
    /**
     * 가격 레벨 계산
     */
    private DistributorComparison.PriceLevel calculatePriceLevel(Integer minOrderAmount) {
        if (minOrderAmount == null) {
            return DistributorComparison.PriceLevel.MEDIUM;
        }
        
        if (minOrderAmount < 50000) {
            return DistributorComparison.PriceLevel.LOW;
        } else if (minOrderAmount < 150000) {
            return DistributorComparison.PriceLevel.MEDIUM;
        } else {
            return DistributorComparison.PriceLevel.HIGH;
        }
    }
    
    /**
     * 배송 속도 계산
     */
    private DistributorComparison.DeliverySpeed calculateDeliverySpeed(String deliveryInfo) {
        if (deliveryInfo == null) {
            return DistributorComparison.DeliverySpeed.TWO_TO_THREE_DAYS;
        }
        
        String info = deliveryInfo.toLowerCase();
        if (info.contains("당일")) {
            return DistributorComparison.DeliverySpeed.SAME_DAY;
        } else if (info.contains("익일")) {
            return DistributorComparison.DeliverySpeed.NEXT_DAY;
        } else if (info.contains("2-3일") || info.contains("2~3일")) {
            return DistributorComparison.DeliverySpeed.TWO_TO_THREE_DAYS;
        } else {
            return DistributorComparison.DeliverySpeed.OVER_THREE_DAYS;
        }
    }
    
    /**
     * 품질 등급 계산
     */
    private DistributorComparison.QualityRating calculateQualityRating(Distributor distributor) {
        int score = 0;
        
        // 인증 보유
        if (distributor.getCertifications() != null && !distributor.getCertifications().isEmpty()) {
            score += distributor.getCertifications().split(",").length * 10;
        }
        
        // 배송 가능
        if (distributor.getDeliveryAvailable() != null && distributor.getDeliveryAvailable()) {
            score += 20;
        }
        
        // 최소 주문 금액이 낮음 (접근성)
        if (distributor.getMinOrderAmount() != null && distributor.getMinOrderAmount() < 100000) {
            score += 10;
        }
        
        if (score >= 40) {
            return DistributorComparison.QualityRating.EXCELLENT;
        } else if (score >= 30) {
            return DistributorComparison.QualityRating.GOOD;
        } else if (score >= 20) {
            return DistributorComparison.QualityRating.AVERAGE;
        } else {
            return DistributorComparison.QualityRating.BELOW_AVERAGE;
        }
    }
    
    /**
     * 신뢰도 점수 계산
     */
    private BigDecimal calculateReliabilityScore(Distributor distributor) {
        BigDecimal score = BigDecimal.ZERO;
        
        // 인증 보유 (최대 40점)
        if (distributor.getCertifications() != null && !distributor.getCertifications().isEmpty()) {
            int certCount = distributor.getCertifications().split(",").length;
            score = score.add(BigDecimal.valueOf(Math.min(certCount * 13, 40)));
        }
        
        // 배송 서비스 (30점)
        if (distributor.getDeliveryAvailable() != null && distributor.getDeliveryAvailable()) {
            score = score.add(BigDecimal.valueOf(30));
        }
        
        // 상세 정보 제공 (30점)
        if (distributor.getDescription() != null && !distributor.getDescription().isEmpty()) {
            score = score.add(BigDecimal.valueOf(15));
        }
        if (distributor.getDeliveryInfo() != null && !distributor.getDeliveryInfo().isEmpty()) {
            score = score.add(BigDecimal.valueOf(15));
        }
        
        return score.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 강점 분석
     */
    private List<String> analyzeStrengths(Distributor distributor, MatchingScore matchingScore) {
        List<String> strengths = new ArrayList<>();
        
        if (matchingScore.getRegionScore().compareTo(BigDecimal.valueOf(90)) >= 0) {
            strengths.add("서비스 지역 완벽 일치");
        }
        
        if (matchingScore.getProductScore().compareTo(BigDecimal.valueOf(80)) >= 0) {
            strengths.add("필요 품목 대부분 공급 가능");
        }
        
        if (distributor.getDeliveryAvailable() != null && distributor.getDeliveryAvailable()) {
            strengths.add("배송 서비스 제공");
        }
        
        if (distributor.getCertifications() != null && distributor.getCertifications().split(",").length >= 2) {
            strengths.add("다수 인증 보유");
        }
        
        if (distributor.getMinOrderAmount() != null && distributor.getMinOrderAmount() < 100000) {
            strengths.add("낮은 최소 주문 금액");
        }
        
        return strengths;
    }
    
    /**
     * 약점 분석
     */
    private List<String> analyzeWeaknesses(Distributor distributor, MatchingScore matchingScore) {
        List<String> weaknesses = new ArrayList<>();
        
        if (matchingScore.getRegionScore().compareTo(BigDecimal.valueOf(50)) < 0) {
            weaknesses.add("서비스 지역 불일치");
        }
        
        if (matchingScore.getProductScore().compareTo(BigDecimal.valueOf(50)) < 0) {
            weaknesses.add("필요 품목 공급 제한적");
        }
        
        if (distributor.getDeliveryAvailable() == null || !distributor.getDeliveryAvailable()) {
            weaknesses.add("배송 서비스 미제공");
        }
        
        if (distributor.getCertifications() == null || distributor.getCertifications().isEmpty()) {
            weaknesses.add("인증 미보유");
        }
        
        if (distributor.getMinOrderAmount() != null && distributor.getMinOrderAmount() >= 200000) {
            weaknesses.add("높은 최소 주문 금액");
        }
        
        return weaknesses;
    }
    
    /**
     * 최고 카테고리 찾기
     */
    private DistributorComparison.ComparisonCategory findBestCategory(MatchingScore matchingScore) {
        Map<DistributorComparison.ComparisonCategory, BigDecimal> scores = new HashMap<>();
        scores.put(DistributorComparison.ComparisonCategory.DELIVERY, matchingScore.getDeliveryScore());
        scores.put(DistributorComparison.ComparisonCategory.CERTIFICATION, matchingScore.getCertificationScore());
        scores.put(DistributorComparison.ComparisonCategory.SERVICE, matchingScore.getRegionScore());
        
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DistributorComparison.ComparisonCategory.SERVICE);
    }
    
    /**
     * 가격 메모 생성
     */
    private String generatePriceNote(Integer minOrderAmount) {
        if (minOrderAmount == null) {
            return "최소 주문 금액 정보 없음";
        }
        return String.format("최소 주문 금액: %,d원", minOrderAmount);
    }
    
    /**
     * 배송비 추출
     */
    private Integer extractDeliveryFee(String deliveryInfo) {
        if (deliveryInfo == null) {
            return null;
        }
        
        if (deliveryInfo.contains("무료")) {
            return 0;
        }
        
        // 간단한 패턴 매칭 (실제로는 더 정교한 파싱 필요)
        return null;
    }
    
    /**
     * 순위 매기기
     */
    private void assignRanks(List<DistributorComparison> comparisons) {
        // 총점 기준 정렬
        List<DistributorComparison> sorted = comparisons.stream()
                .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
                .collect(Collectors.toList());
        
        // 순위 할당
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setRank(i + 1);
        }
    }
}
