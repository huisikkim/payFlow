package com.example.payflow.settlement.application;

import com.example.payflow.settlement.domain.*;
import com.example.payflow.settlement.presentation.dto.DailySettlementResponse;
import com.example.payflow.settlement.presentation.dto.SettlementStatisticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailySettlementService {
    
    private final DailySettlementRepository dailySettlementRepository;
    private final IngredientSettlementRepository settlementRepository;
    
    /**
     * 정산 완료 시 일일 정산 업데이트
     */
    @Transactional
    public void updatePayment(IngredientSettlement settlement, Long paidAmount) {
        LocalDate settlementDate = settlement.getSettlementDate().toLocalDate();
        
        DailySettlement dailySettlement = dailySettlementRepository
            .findBySettlementDateAndStoreIdAndDistributorId(
                settlementDate, 
                settlement.getStoreId(), 
                settlement.getDistributorId())
            .orElseThrow(() -> new IllegalStateException(
                "일일 정산을 찾을 수 없습니다: " + settlementDate + ", " + settlement.getStoreId()));
        
        dailySettlement.updatePayment(paidAmount);
        dailySettlementRepository.save(dailySettlement);
        
        log.info("📊 일일 정산 지불 업데이트: date={}, store={}, paidAmount={}", 
            settlementDate, settlement.getStoreId(), paidAmount);
    }
    
    /**
     * 정산 생성 시 일일 정산에 반영
     */
    @Transactional
    public void aggregateSettlement(IngredientSettlement settlement) {
        LocalDate settlementDate = settlement.getSettlementDate().toLocalDate();
        
        DailySettlement dailySettlement = dailySettlementRepository
            .findBySettlementDateAndStoreIdAndDistributorId(
                settlementDate, 
                settlement.getStoreId(), 
                settlement.getDistributorId())
            .orElseGet(() -> {
                DailySettlement newDaily = new DailySettlement(
                    settlementDate,
                    settlement.getStoreId(),
                    settlement.getDistributorId()
                );
                return dailySettlementRepository.save(newDaily);
            });
        
        dailySettlement.addOrder(
            settlement.getOrderType(),
            settlement.getSettlementAmount(),
            settlement.getPaidAmount(),
            settlement.getOutstandingAmount()
        );
        
        dailySettlementRepository.save(dailySettlement);
        
        log.info("📊 일일 정산 집계 완료: date={}, store={}, distributor={}, amount={}", 
            settlementDate, settlement.getStoreId(), settlement.getDistributorId(), 
            settlement.getSettlementAmount());
    }
    
    /**
     * 특정 날짜의 일일 정산 재집계 (배치용)
     */
    @Transactional
    public void recalculateDailySettlement(LocalDate targetDate) {
        log.info("🔄 일일 정산 재집계 시작: {}", targetDate);
        
        // 해당 날짜의 모든 정산 조회
        List<IngredientSettlement> settlements = settlementRepository
            .findBySettlementDateBetween(
                targetDate.atStartOfDay(), 
                targetDate.plusDays(1).atStartOfDay()
            );
        
        log.info("📋 재집계 대상 정산 건수: {}", settlements.size());
        
        // 기존 일일 정산 삭제
        settlements.stream()
            .map(s -> new String[]{s.getStoreId(), s.getDistributorId()})
            .distinct()
            .forEach(pair -> {
                dailySettlementRepository
                    .findBySettlementDateAndStoreIdAndDistributorId(targetDate, pair[0], pair[1])
                    .ifPresent(dailySettlementRepository::delete);
            });
        
        // 재집계
        settlements.forEach(this::aggregateSettlement);
        
        log.info("✅ 일일 정산 재집계 완료: {}", targetDate);
    }
    
    /**
     * 가게별 일일 정산 조회
     */
    @Transactional(readOnly = true)
    public List<DailySettlementResponse> getDailySettlementsByStore(
            String storeId, LocalDate startDate, LocalDate endDate) {
        
        List<DailySettlement> settlements = dailySettlementRepository
            .findByStoreIdAndSettlementDateBetweenOrderBySettlementDateDesc(
                storeId, startDate, endDate);
        
        return settlements.stream()
            .map(DailySettlementResponse::from)
            .collect(Collectors.toList());
    }
    
    /**
     * 유통업자별 일일 정산 조회
     */
    @Transactional(readOnly = true)
    public List<DailySettlementResponse> getDailySettlementsByDistributor(
            String distributorId, LocalDate startDate, LocalDate endDate) {
        
        List<DailySettlement> settlements = dailySettlementRepository
            .findByDistributorIdAndSettlementDateBetweenOrderBySettlementDateDesc(
                distributorId, startDate, endDate);
        
        return settlements.stream()
            .map(DailySettlementResponse::from)
            .collect(Collectors.toList());
    }
    
    /**
     * 가게별 정산 통계
     */
    @Transactional(readOnly = true)
    public SettlementStatisticsResponse getStoreStatistics(
            String storeId, LocalDate startDate, LocalDate endDate) {
        
        List<DailySettlement> settlements = dailySettlementRepository
            .findByStoreIdAndSettlementDateBetweenOrderBySettlementDateDesc(
                storeId, startDate, endDate);
        
        return calculateStatistics(settlements, "STORE", storeId);
    }
    
    /**
     * 유통업자별 정산 통계
     */
    @Transactional(readOnly = true)
    public SettlementStatisticsResponse getDistributorStatistics(
            String distributorId, LocalDate startDate, LocalDate endDate) {
        
        List<DailySettlement> settlements = dailySettlementRepository
            .findByDistributorIdAndSettlementDateBetweenOrderBySettlementDateDesc(
                distributorId, startDate, endDate);
        
        return calculateStatistics(settlements, "DISTRIBUTOR", distributorId);
    }
    
    private SettlementStatisticsResponse calculateStatistics(
            List<DailySettlement> settlements, String type, String id) {
        
        int totalOrderCount = settlements.stream()
            .mapToInt(DailySettlement::getOrderCount)
            .sum();
        
        long totalSalesAmount = settlements.stream()
            .mapToLong(DailySettlement::getTotalSalesAmount)
            .sum();
        
        long totalPaidAmount = settlements.stream()
            .mapToLong(DailySettlement::getTotalPaidAmount)
            .sum();
        
        long totalOutstandingAmount = settlements.stream()
            .mapToLong(DailySettlement::getTotalOutstandingAmount)
            .sum();
        
        int catalogOrderCount = settlements.stream()
            .mapToInt(DailySettlement::getCatalogOrderCount)
            .sum();
        
        long catalogSalesAmount = settlements.stream()
            .mapToLong(DailySettlement::getCatalogSalesAmount)
            .sum();
        
        int ingredientOrderCount = settlements.stream()
            .mapToInt(DailySettlement::getIngredientOrderCount)
            .sum();
        
        long ingredientSalesAmount = settlements.stream()
            .mapToLong(DailySettlement::getIngredientSalesAmount)
            .sum();
        
        double paymentRate = totalSalesAmount > 0 
            ? (double) totalPaidAmount / totalSalesAmount * 100 
            : 0.0;
        
        return SettlementStatisticsResponse.builder()
            .type(type)
            .id(id)
            .totalOrderCount(totalOrderCount)
            .totalSalesAmount(totalSalesAmount)
            .totalPaidAmount(totalPaidAmount)
            .totalOutstandingAmount(totalOutstandingAmount)
            .catalogOrderCount(catalogOrderCount)
            .catalogSalesAmount(catalogSalesAmount)
            .ingredientOrderCount(ingredientOrderCount)
            .ingredientSalesAmount(ingredientSalesAmount)
            .paymentRate(paymentRate)
            .build();
    }
}
