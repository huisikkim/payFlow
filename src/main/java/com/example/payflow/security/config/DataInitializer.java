package com.example.payflow.security.config;

import com.example.payflow.inventory.application.InventoryService;
import com.example.payflow.security.domain.Role;
import com.example.payflow.security.domain.User;
import com.example.payflow.security.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InventoryService inventoryService;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                // 일반 사용자 생성
                User user = User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("password"))
                        .email("user@example.com")
                        .roles(Set.of(Role.ROLE_USER))
                        .enabled(true)
                        .build();
                userRepository.save(user);
                log.info("Created default user: username=user, password=password");
                
                // 관리자 생성
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("admin@example.com")
                        .roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.info("Created default admin: username=admin, password=admin");
            }
            
            // Saga 테스트용 재고 데이터 생성
            try {
                log.info("🔧 Saga 테스트용 초기 데이터 생성 중...");
                inventoryService.createInventory("PROD-TEST-001", "테스트 상품 1", 100);
                inventoryService.createInventory("PROD-TEST-002", "테스트 상품 2", 50);
                inventoryService.createInventory("PROD-TEST-003", "테스트 상품 3", 0);
                log.info("✅ Saga 테스트용 초기 데이터 생성 완료");
            } catch (Exception e) {
                log.warn("초기 데이터 생성 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
            }
        };
    }
}
