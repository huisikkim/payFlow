package com.example.payflow.parlevel.application;

import com.example.payflow.parlevel.domain.ConsumptionPattern;
import com.example.payflow.parlevel.domain.ConsumptionPatternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumptionAnalysisService {
    
    private final ConsumptionPatternRepository consumptionRepository;
    
    @Transactional
    public void recordConsumption(String storeId, String itemName, Integer quantity, String unit) {
        ConsumptionPattern pattern = new ConsumptionPattern(
            storeId, itemName, LocalDate.now(), quantity, unit
        );
        consumptionRepository.save(pattern);
        log.info("📊 소비 패턴 기록: storeId={}, itemName={}, quantity={}", 
            storeId, itemName, quantity);
    }
    
    @Transactional(readOnly = true)
    public Double calculateAverageDailyConsumption(String storeId, String itemName, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        Double avg = consumptionRepository.calculateAverageConsumption(storeId, itemName, startDate);
        return avg != null ? avg : 0.0;
    }
    
    @Transactional(readOnly = true)
    public Double calculateWeekdayAverage(String storeId, String itemName, DayOfWeek dayOfWeek) {
        LocalDate startDate = LocalDate.now().minusDays(30);
        Double avg = consumptionRepository.calculateAverageByDayOfWeek(
            storeId, itemName, dayOfWeek, startDate
        );
        return avg != null ? avg : 0.0;
    }
    
    @Transactional(readOnly = true)
    public Double calculateStandardDeviation(String storeId, String itemName, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        Double stdDev = consumptionRepository.calculateStandardDeviation(storeId, itemName, startDate);
        return stdDev != null ? stdDev : 0.0;
    }
    
    @Transactional(readOnly = true)
    public Integer predictConsumption(String storeId, String itemName, int forecastDays) {
        // 최근 30일 평균 소비량
        Double avg30Days = calculateAverageDailyConsumption(storeId, itemName, 30);
        
        // 최근 7일 평균 소비량 (더 높은 가중치)
        Double avg7Days = calculateAverageDailyConsumption(storeId, itemName, 7);
        
        // 가중 평균: 최근 7일 70%, 30일 30%
        double weightedAvg = (avg7Days * 0.7) + (avg30Days * 0.3);
        
        // 예측 기간 동안의 총 소비량
        int predictedTotal = (int) Math.ceil(weightedAvg * forecastDays);
        
        log.info("📈 소비 예측: storeId={}, itemName={}, forecastDays={}, predicted={}", 
            storeId, itemName, forecastDays, predictedTotal);
        
        return predictedTotal;
    }
    
    @Transactional(readOnly = true)
    public List<ConsumptionPattern> getConsumptionHistory(String storeId, String itemName, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        LocalDate endDate = LocalDate.now();
        return consumptionRepository.findByStoreIdAndItemNameAndConsumptionDateBetween(
            storeId, itemName, startDate, endDate
        );
    }
}
