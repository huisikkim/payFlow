package com.example.payflow.parlevel.application;

import com.example.payflow.parlevel.domain.ParLevel;
import com.example.payflow.parlevel.domain.ParLevelRepository;
import com.example.payflow.parlevel.presentation.dto.CreateParLevelRequest;
import com.example.payflow.parlevel.presentation.dto.ParLevelResponse;
import com.example.payflow.parlevel.presentation.dto.UpdateParLevelRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParLevelService {
    
    private final ParLevelRepository parLevelRepository;
    private final ConsumptionAnalysisService consumptionAnalysisService;
    
    @Transactional
    public ParLevelResponse createParLevel(CreateParLevelRequest request) {
        ParLevel parLevel = new ParLevel(
            request.getStoreId(),
            request.getItemName(),
            request.getUnit(),
            request.getMinLevel(),
            request.getMaxLevel(),
            request.getSafetyStock(),
            request.getLeadTimeDays(),
            request.getAutoOrderEnabled()
        );
        
        parLevelRepository.save(parLevel);
        log.info("✅ Par Level 생성: storeId={}, itemName={}, min={}, max={}", 
            request.getStoreId(), request.getItemName(), 
            request.getMinLevel(), request.getMaxLevel());
        
        return ParLevelResponse.from(parLevel);
    }
    
    @Transactional
    public ParLevelResponse updateParLevel(Long id, UpdateParLevelRequest request) {
        ParLevel parLevel = parLevelRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Par Level을 찾을 수 없습니다: " + id));
        
        parLevel.updateLevels(request.getMinLevel(), request.getMaxLevel(), request.getSafetyStock());
        
        if (request.getLeadTimeDays() != null) {
            parLevel.updateLeadTime(request.getLeadTimeDays());
        }
        
        log.info("✅ Par Level 수정: id={}, min={}, max={}", id, request.getMinLevel(), request.getMaxLevel());
        
        return ParLevelResponse.from(parLevel);
    }
    
    @Transactional
    public void enableAutoOrder(Long id) {
        ParLevel parLevel = parLevelRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Par Level을 찾을 수 없습니다: " + id));
        
        parLevel.enableAutoOrder();
        log.info("✅ 자동 발주 활성화: id={}, itemName={}", id, parLevel.getItemName());
    }
    
    @Transactional
    public void disableAutoOrder(Long id) {
        ParLevel parLevel = parLevelRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Par Level을 찾을 수 없습니다: " + id));
        
        parLevel.disableAutoOrder();
        log.info("✅ 자동 발주 비활성화: id={}, itemName={}", id, parLevel.getItemName());
    }
    
    @Transactional(readOnly = true)
    public ParLevelResponse getParLevel(String storeId, String itemName) {
        ParLevel parLevel = parLevelRepository.findByStoreIdAndItemName(storeId, itemName)
            .orElseThrow(() -> new IllegalArgumentException(
                "Par Level을 찾을 수 없습니다: " + storeId + ", " + itemName));
        
        return ParLevelResponse.from(parLevel);
    }
    
    @Transactional(readOnly = true)
    public List<ParLevelResponse> getParLevelsByStore(String storeId) {
        return parLevelRepository.findByStoreId(storeId).stream()
            .map(ParLevelResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ParLevelResponse autoCalculateParLevel(String storeId, String itemName, String unit, Integer leadTimeDays) {
        // 최근 30일 평균 소비량
        Double avgDaily = consumptionAnalysisService.calculateAverageDailyConsumption(storeId, itemName, 30);
        
        // 표준편차 (변동성)
        Double stdDev = consumptionAnalysisService.calculateStandardDeviation(storeId, itemName, 30);
        
        // 안전 재고 = 리드타임 동안 소비량 + (표준편차 * 1.65) - 95% 신뢰수준
        int safetyStock = (int) Math.ceil((avgDaily * leadTimeDays) + (stdDev * 1.65));
        
        // 최소 재고 = 리드타임 동안 소비량 + 안전 재고
        int minLevel = (int) Math.ceil(avgDaily * leadTimeDays) + safetyStock;
        
        // 최대 재고 = 최소 재고 + (평균 일일 소비량 * 7일)
        int maxLevel = minLevel + (int) Math.ceil(avgDaily * 7);
        
        ParLevel parLevel = new ParLevel(
            storeId, itemName, unit, minLevel, maxLevel, safetyStock, leadTimeDays, true
        );
        
        parLevelRepository.save(parLevel);
        log.info("✅ Par Level 자동 계산: storeId={}, itemName={}, min={}, max={}, safety={}", 
            storeId, itemName, minLevel, maxLevel, safetyStock);
        
        return ParLevelResponse.from(parLevel);
    }
}
