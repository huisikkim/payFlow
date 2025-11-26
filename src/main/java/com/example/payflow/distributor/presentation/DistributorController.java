package com.example.payflow.distributor.presentation;

import com.example.payflow.distributor.application.DistributorService;
import com.example.payflow.distributor.domain.Distributor;
import com.example.payflow.distributor.presentation.dto.DistributorInfoRequest;
import com.example.payflow.distributor.presentation.dto.DistributorInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/distributor")
@RequiredArgsConstructor
public class DistributorController {
    
    private final DistributorService distributorService;
    
    /**
     * 유통업체 정보 등록/수정
     * POST /api/distributor/info
     */
    @PostMapping("/info")
    @PreAuthorize("hasAuthority('ROLE_DISTRIBUTOR')")
    public ResponseEntity<DistributorInfoResponse> registerDistributorInfo(@RequestBody DistributorInfoRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // distributorId가 없으면 username 사용
        String distributorId = request.getDistributorId() != null ? request.getDistributorId() : username;
        
        Distributor distributor = distributorService.registerOrUpdateDistributorInfo(
                distributorId,
                request.getDistributorName(),
                request.getSupplyProducts(),
                request.getServiceRegions(),
                request.getDeliveryAvailable(),
                request.getDeliveryInfo(),
                request.getDescription(),
                request.getCertifications(),
                request.getMinOrderAmount(),
                request.getOperatingHours(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getAddress()
        );
        
        return ResponseEntity.ok(DistributorInfoResponse.from(distributor));
    }
    
    /**
     * 내 유통업체 정보 조회
     * GET /api/distributor/info
     */
    @GetMapping("/info")
    @PreAuthorize("hasAuthority('ROLE_DISTRIBUTOR')")
    public ResponseEntity<DistributorInfoResponse> getDistributorInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            Distributor distributor = distributorService.getDistributor(username);
            return ResponseEntity.ok(DistributorInfoResponse.from(distributor));
        } catch (IllegalArgumentException e) {
            // 유통업체 정보가 없으면 404
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 특정 유통업체 정보 조회 (공개 API - 매장이 유통업체 정보 확인용)
     * GET /api/distributor/info/{distributorId}
     */
    @GetMapping("/info/{distributorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_OWNER', 'ROLE_DISTRIBUTOR')")
    public ResponseEntity<DistributorInfoResponse> getDistributorInfoById(@PathVariable String distributorId) {
        try {
            Distributor distributor = distributorService.getDistributor(distributorId);
            return ResponseEntity.ok(DistributorInfoResponse.from(distributor));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 유통업체 활성화/비활성화
     * PUT /api/distributor/status
     */
    @PutMapping("/status")
    @PreAuthorize("hasAuthority('ROLE_DISTRIBUTOR')")
    public ResponseEntity<String> toggleDistributorStatus(@RequestParam boolean activate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        distributorService.toggleDistributorStatus(username, activate);
        
        String message = activate ? "유통업체가 활성화되었습니다." : "유통업체가 비활성화되었습니다.";
        return ResponseEntity.ok(message);
    }
}
