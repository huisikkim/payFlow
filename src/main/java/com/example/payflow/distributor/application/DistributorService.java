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
}
