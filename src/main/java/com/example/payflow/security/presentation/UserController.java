package com.example.payflow.security.presentation;

import com.example.payflow.security.domain.User;
import com.example.payflow.security.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    
    private final UserRepository userRepository;
    
    @GetMapping("/user/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 사용자 정보 조회
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("userType", user.getUserType());
        profile.put("businessNumber", user.getBusinessNumber());
        profile.put("businessName", user.getBusinessName());
        profile.put("ownerName", user.getOwnerName());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("address", user.getAddress());
        profile.put("roles", user.getRoles());
        profile.put("enabled", user.isEnabled());
        profile.put("createdAt", user.getCreatedAt());
        
        return ResponseEntity.ok(profile);
    }
    
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getAdminDashboard() {
        Map<String, String> dashboard = new HashMap<>();
        dashboard.put("message", "Welcome to Admin Dashboard");
        dashboard.put("access", "ADMIN_ONLY");
        
        return ResponseEntity.ok(dashboard);
    }
    
    // 매장 사장님 전용 대시보드 (예시)
    @GetMapping("/store/dashboard")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<Map<String, String>> getStoreDashboard() {
        Map<String, String> dashboard = new HashMap<>();
        dashboard.put("message", "매장 사장님 대시보드");
        dashboard.put("access", "STORE_OWNER_ONLY");
        
        return ResponseEntity.ok(dashboard);
    }
    
    // 유통업체 전용 대시보드 (예시)
    @GetMapping("/distributor/dashboard")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<Map<String, String>> getDistributorDashboard() {
        Map<String, String> dashboard = new HashMap<>();
        dashboard.put("message", "유통업체 대시보드");
        dashboard.put("access", "DISTRIBUTOR_ONLY");
        
        return ResponseEntity.ok(dashboard);
    }
    
    // 매장 사장님 또는 유통업체 모두 접근 가능한 API (예시)
    @GetMapping("/marketplace/products")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<Map<String, String>> getMarketplaceProducts() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "마켓플레이스 상품 목록");
        
        return ResponseEntity.ok(response);
    }
}
