package com.example.payflow.store.presentation;

import com.example.payflow.store.application.StoreService;
import com.example.payflow.store.domain.Store;
import com.example.payflow.store.presentation.dto.StoreInfoRequest;
import com.example.payflow.store.presentation.dto.StoreInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {
    
    private final StoreService storeService;
    
    /**
     * 매장 정보 등록/수정
     * POST /api/store/info
     */
    @PostMapping("/info")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<StoreInfoResponse> registerStoreInfo(@RequestBody StoreInfoRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // storeId가 없으면 username 사용
        String storeId = request.getStoreId() != null ? request.getStoreId() : username;
        
        Store store = storeService.registerOrUpdateStoreInfo(
                storeId,
                request.getStoreName(),
                username,  // ownerName은 로그인한 사용자
                request.getBusinessType(),
                request.getRegion(),
                request.getMainProducts(),
                request.getDescription(),
                request.getEmployeeCount(),
                request.getOperatingHours(),
                request.getPhoneNumber(),
                request.getAddress()
        );
        
        return ResponseEntity.ok(StoreInfoResponse.from(store));
    }
    
    /**
     * 매장 정보 조회
     * GET /api/store/info
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<StoreInfoResponse> getStoreInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            Store store = storeService.getStore(username);
            return ResponseEntity.ok(StoreInfoResponse.from(store));
        } catch (IllegalArgumentException e) {
            // 매장 정보가 없으면 404
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 특정 매장 정보 조회 (공개 API - 유통업체가 매장 정보 확인용)
     * GET /api/store/info/{storeId}
     */
    @GetMapping("/info/{storeId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<StoreInfoResponse> getStoreInfoById(@PathVariable String storeId) {
        try {
            Store store = storeService.getStore(storeId);
            return ResponseEntity.ok(StoreInfoResponse.from(store));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 매장 활성화/비활성화
     * PUT /api/store/status
     */
    @PutMapping("/status")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<String> toggleStoreStatus(@RequestParam boolean activate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        storeService.toggleStoreStatus(username, activate);
        
        String message = activate ? "매장이 활성화되었습니다." : "매장이 비활성화되었습니다.";
        return ResponseEntity.ok(message);
    }
}
