package com.example.payflow.security.presentation;

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
    
    @GetMapping("/user/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", authentication.getName());
        profile.put("authorities", authentication.getAuthorities());
        
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
}
