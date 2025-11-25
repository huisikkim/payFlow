package com.example.payflow.distributor.application;

import com.example.payflow.distributor.domain.Distributor;
import com.example.payflow.distributor.domain.DistributorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributorService {
    
    private final DistributorRepository distributorRepository;
    
    @Transactional
    public Distributor createDistributor(String distributorId, String distributorName, 
                                        String businessNumber, String phoneNumber, String managerName) {
        Distributor distributor = new Distributor(distributorId, distributorName, 
            businessNumber, phoneNumber, managerName);
        distributorRepository.save(distributor);
        log.info("🚚 유통사 생성: distributorId={}, distributorName={}", distributorId, distributorName);
        return distributor;
    }
    
    @Transactional(readOnly = true)
    public Distributor getDistributor(String distributorId) {
        return distributorRepository.findByDistributorId(distributorId)
            .orElseThrow(() -> new IllegalArgumentException("유통사를 찾을 수 없습니다: " + distributorId));
    }
    
    /**
     * 유통업체 상세 정보 등록/수정
     */
    @Transactional
    public Distributor registerOrUpdateDistributorInfo(String distributorId, String distributorName,
                                                      String supplyProducts, String serviceRegions,
                                                      Boolean deliveryAvailable, String deliveryInfo,
                                                      String description, String certifications,
                                                      Integer minOrderAmount, String operatingHours,
                                                      String phoneNumber, String email, String address) {
        Distributor distributor = distributorRepository.findByDistributorId(distributorId)
                .orElseGet(() -> {
                    Distributor newDistributor = new Distributor(distributorId, distributorName, 
                            null, phoneNumber, null);
                    log.info("🚚 새 유통업체 생성: distributorId={}, distributorName={}", 
                            distributorId, distributorName);
                    return newDistributor;
                });
        
        // 기본 정보 업데이트
        distributor.updateBasicInfo(distributorName, phoneNumber, email);
        
        // 상세 정보 업데이트
        distributor.updateDistributorInfo(supplyProducts, serviceRegions, deliveryAvailable,
                deliveryInfo, description, certifications, minOrderAmount, operatingHours, address);
        
        distributorRepository.save(distributor);
        log.info("✅ 유통업체 정보 업데이트: distributorId={}, supplyProducts={}, serviceRegions={}", 
                distributorId, supplyProducts, serviceRegions);
        
        return distributor;
    }
    
    /**
     * 유통업체 활성화/비활성화
     */
    @Transactional
    public void toggleDistributorStatus(String distributorId, boolean activate) {
        Distributor distributor = getDistributor(distributorId);
        if (activate) {
            distributor.activate();
            log.info("✅ 유통업체 활성화: distributorId={}", distributorId);
        } else {
            distributor.deactivate();
            log.info("⛔ 유통업체 비활성화: distributorId={}", distributorId);
        }
        distributorRepository.save(distributor);
    }
}
