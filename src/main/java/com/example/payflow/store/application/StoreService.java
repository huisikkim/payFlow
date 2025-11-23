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
}
