package com.example.payflow.store.application;

import com.example.payflow.store.domain.Store;
import com.example.payflow.store.domain.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {
    
    private final StoreRepository storeRepository;
    
    @Transactional
    public Store createStore(String storeId, String storeName, String ownerName, 
                            String phoneNumber, String address) {
        Store store = new Store(storeId, storeName, ownerName, phoneNumber, address);
        storeRepository.save(store);
        log.info("🏪 매장 생성: storeId={}, storeName={}", storeId, storeName);
        return store;
    }
    
    @Transactional(readOnly = true)
    public Store getStore(String storeId) {
        return storeRepository.findByStoreId(storeId)
            .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
    }
    
    /**
     * 매장 상세 정보 등록/수정
     */
    @Transactional
    public Store registerOrUpdateStoreInfo(String storeId, String storeName, String ownerName,
                                          String businessType, String region, String mainProducts,
                                          String description, Integer employeeCount, String operatingHours,
                                          String phoneNumber, String address) {
        Store store = storeRepository.findByStoreId(storeId)
                .orElseGet(() -> {
                    Store newStore = new Store(storeId, storeName, ownerName, phoneNumber, address);
                    log.info("🏪 새 매장 생성: storeId={}, storeName={}", storeId, storeName);
                    return newStore;
                });
        
        // 기본 정보 업데이트
        store.updateBasicInfo(storeName, phoneNumber, address);
        
        // 상세 정보 업데이트
        store.updateStoreInfo(businessType, region, mainProducts, description, employeeCount, operatingHours);
        
        storeRepository.save(store);
        log.info("✅ 매장 정보 업데이트: storeId={}, businessType={}, region={}", 
                storeId, businessType, region);
        
        return store;
    }
    
    /**
     * 매장 활성화/비활성화
     */
    @Transactional
    public void toggleStoreStatus(String storeId, boolean activate) {
        Store store = getStore(storeId);
        if (activate) {
            store.activate();
            log.info("✅ 매장 활성화: storeId={}", storeId);
        } else {
            store.deactivate();
            log.info("⛔ 매장 비활성화: storeId={}", storeId);
        }
        storeRepository.save(store);
    }
}
