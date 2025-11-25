package com.example.payflow.matching.application;

import com.example.payflow.distributor.domain.Distributor;
import com.example.payflow.distributor.domain.DistributorRepository;
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
public class MatchingService {
    
    private final StoreRepository storeRepository;
    private final DistributorRepository distributorRepository;
    
    /**
     * 매장에 맞는 유통업체 추천
     */
    @Transactional(readOnly = true)
    public List<MatchingScore> recommendDistributors(String storeId, int limit) {
        // 매장 정보 조회
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        // 활성화된 유통업체 목록 조회
        List<Distributor> distributors = distributorRepository.findAll().stream()
                .filter(d -> d.getIsActive() != null && d.getIsActive())
                .collect(Collectors.toList());
        
        if (distributors.isEmpty()) {
            log.warn("활성화된 유통업체가 없습니다");
            return Collections.emptyList();
        }
        
        // 각 유통업체별 매칭 점수 계산
        List<MatchingScore> scores = distributors.stream()
                .map(distributor -> calculateMatchingScore(store, distributor))
                .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
                .limit(limit)
                .collect(Collectors.toList());
        
        log.info("✅ 매장 {} 에 대한 유통업체 추천 완료: {}개", storeId, scores.size());
        
        return scores;
    }
    
    /**
     * 매칭 점수 계산
     */
    private MatchingScore calculateMatchingScore(Store store, Distributor distributor) {
        BigDecimal regionScore = calculateRegionScore(store, distributor);
        BigDecimal productScore = calculateProductScore(store, distributor);
        BigDecimal deliveryScore = calculateDeliveryScore(distributor);
        BigDecimal certificationScore = calculateCertificationScore(distributor);
        
        // 가중치 적용 총점 계산
        // 지역: 40%, 품목: 35%, 배송: 15%, 인증: 10%
        BigDecimal totalScore = regionScore.multiply(BigDecimal.valueOf(0.4))
                .add(productScore.multiply(BigDecimal.valueOf(0.35)))
                .add(deliveryScore.multiply(BigDecimal.valueOf(0.15)))
                .add(certificationScore.multiply(BigDecimal.valueOf(0.10)))
                .setScale(2, RoundingMode.HALF_UP);
        
        String matchReason = generateMatchReason(regionScore, productScore, deliveryScore, certificationScore);
        
        return MatchingScore.builder()
                .distributorId(distributor.getDistributorId())
                .distributorName(distributor.getDistributorName())
                .totalScore(totalScore)
                .regionScore(regionScore)
                .productScore(productScore)
                .deliveryScore(deliveryScore)
                .certificationScore(certificationScore)
                .matchReason(matchReason)
                .supplyProducts(distributor.getSupplyProducts())
                .serviceRegions(distributor.getServiceRegions())
                .deliveryAvailable(distributor.getDeliveryAvailable())
                .deliveryInfo(distributor.getDeliveryInfo())
                .certifications(distributor.getCertifications())
                .minOrderAmount(distributor.getMinOrderAmount())
                .phoneNumber(distributor.getPhoneNumber())
                .email(distributor.getEmail())
                .build();
    }
    
    /**
     * 지역 매칭 점수 계산
     */
    private BigDecimal calculateRegionScore(Store store, Distributor distributor) {
        if (store.getRegion() == null || distributor.getServiceRegions() == null) {
            return BigDecimal.ZERO;
        }
        
        String storeRegion = store.getRegion();
        List<String> serviceRegions = Arrays.asList(distributor.getServiceRegions().split(","));
        
        // 정확히 일치하는 지역이 있으면 100점
        for (String region : serviceRegions) {
            if (storeRegion.contains(region.trim()) || region.trim().contains(storeRegion)) {
                return BigDecimal.valueOf(100);
            }
        }
        
        // 부분 일치 (예: "서울 강남구"와 "서울")
        String storeCity = storeRegion.split(" ")[0]; // "서울"
        for (String region : serviceRegions) {
            if (region.trim().startsWith(storeCity)) {
                return BigDecimal.valueOf(70);
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 품목 매칭 점수 계산
     */
    private BigDecimal calculateProductScore(Store store, Distributor distributor) {
        if (store.getMainProducts() == null || distributor.getSupplyProducts() == null) {
            return BigDecimal.ZERO;
        }
        
        List<String> storeProducts = Arrays.asList(store.getMainProducts().split(","));
        List<String> supplyProducts = Arrays.asList(distributor.getSupplyProducts().split(","));
        
        // 매장이 필요한 품목 중 유통업체가 공급 가능한 품목 개수
        long matchCount = storeProducts.stream()
                .map(String::trim)
                .filter(storeProduct -> supplyProducts.stream()
                        .anyMatch(supplyProduct -> supplyProduct.trim().equals(storeProduct)))
                .count();
        
        if (storeProducts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 매칭 비율로 점수 계산 (0-100)
        double matchRatio = (double) matchCount / storeProducts.size();
        return BigDecimal.valueOf(matchRatio * 100).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 배송 점수 계산
     */
    private BigDecimal calculateDeliveryScore(Distributor distributor) {
        if (distributor.getDeliveryAvailable() == null || !distributor.getDeliveryAvailable()) {
            return BigDecimal.ZERO;
        }
        
        // 배송 가능하면 기본 70점
        BigDecimal score = BigDecimal.valueOf(70);
        
        // 배송 정보가 상세하면 추가 점수
        if (distributor.getDeliveryInfo() != null && !distributor.getDeliveryInfo().isEmpty()) {
            score = score.add(BigDecimal.valueOf(30));
        }
        
        return score;
    }
    
    /**
     * 인증 점수 계산
     */
    private BigDecimal calculateCertificationScore(Distributor distributor) {
        if (distributor.getCertifications() == null || distributor.getCertifications().isEmpty()) {
            return BigDecimal.valueOf(50); // 인증 없어도 기본 50점
        }
        
        List<String> certs = Arrays.asList(distributor.getCertifications().split(","));
        
        // 인증 개수에 따라 점수 부여 (최대 100점)
        int certCount = certs.size();
        if (certCount >= 3) {
            return BigDecimal.valueOf(100);
        } else if (certCount == 2) {
            return BigDecimal.valueOf(85);
        } else if (certCount == 1) {
            return BigDecimal.valueOf(70);
        }
        
        return BigDecimal.valueOf(50);
    }
    
    /**
     * 추천 이유 생성
     */
    private String generateMatchReason(BigDecimal regionScore, BigDecimal productScore, 
                                       BigDecimal deliveryScore, BigDecimal certificationScore) {
        List<String> reasons = new ArrayList<>();
        
        if (regionScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            reasons.add("서비스 지역 완벽 일치");
        } else if (regionScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            reasons.add("서비스 지역 근접");
        }
        
        if (productScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            reasons.add("필요 품목 대부분 공급 가능");
        } else if (productScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            reasons.add("주요 품목 공급 가능");
        }
        
        if (deliveryScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            reasons.add("배송 서비스 우수");
        } else if (deliveryScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            reasons.add("배송 가능");
        }
        
        if (certificationScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            reasons.add("다수 인증 보유");
        } else if (certificationScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            reasons.add("인증 보유");
        }
        
        return reasons.isEmpty() ? "기본 매칭" : String.join(", ", reasons);
    }
    
    /**
     * 특정 품목을 공급하는 유통업체 검색
     */
    @Transactional(readOnly = true)
    public List<MatchingScore> searchDistributorsByProduct(String storeId, String productKeyword) {
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        List<Distributor> distributors = distributorRepository.findAll().stream()
                .filter(d -> d.getIsActive() != null && d.getIsActive())
                .filter(d -> d.getSupplyProducts() != null && 
                            d.getSupplyProducts().contains(productKeyword))
                .collect(Collectors.toList());
        
        return distributors.stream()
                .map(distributor -> calculateMatchingScore(store, distributor))
                .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 지역의 유통업체 검색
     */
    @Transactional(readOnly = true)
    public List<MatchingScore> searchDistributorsByRegion(String storeId, String regionKeyword) {
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        List<Distributor> distributors = distributorRepository.findAll().stream()
                .filter(d -> d.getIsActive() != null && d.getIsActive())
                .filter(d -> d.getServiceRegions() != null && 
                            d.getServiceRegions().contains(regionKeyword))
                .collect(Collectors.toList());
        
        return distributors.stream()
                .map(distributor -> calculateMatchingScore(store, distributor))
                .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
                .collect(Collectors.toList());
    }
}
