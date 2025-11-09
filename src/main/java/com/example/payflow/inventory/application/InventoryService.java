package com.example.payflow.inventory.application;

import com.example.payflow.inventory.domain.Inventory;
import com.example.payflow.inventory.domain.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    
    @Transactional
    public Inventory createInventory(String productId, String productName, Integer quantity) {
        Inventory inventory = new Inventory(productId, productName, quantity);
        return inventoryRepository.save(inventory);
    }
    
    @Transactional(readOnly = true)
    public Inventory getInventory(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }
}
