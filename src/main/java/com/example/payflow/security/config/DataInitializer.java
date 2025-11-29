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
                
                // 매장 사용자 생성
                User store001 = User.builder()
                        .username("store001")
                        .password(passwordEncoder.encode("password"))
                        .email("store001@example.com")
                        .roles(Set.of(Role.ROLE_STORE_OWNER))
                        .enabled(true)
                        .build();
                userRepository.save(store001);
                log.info("Created store user: username=store001, password=password");
                
                // 유통업자 생성
                User dist001 = User.builder()
                        .username("dist001")
                        .password(passwordEncoder.encode("password"))
                        .email("dist001@example.com")
                        .roles(Set.of(Role.ROLE_DISTRIBUTOR))
                        .enabled(true)
                        .build();
                userRepository.save(dist001);
                log.info("Created distributor user: username=dist001, password=password");
                
                // 가게사장님 계정 생성 (kimceo)
                User kimceo = User.builder()
                        .username("kimceo")
                        .password(passwordEncoder.encode("123456"))
                        .email("kimceo@example.com")
                        .userType(com.example.payflow.security.domain.UserType.STORE_OWNER)
                        .roles(Set.of(Role.ROLE_STORE_OWNER))
                        .businessNumber("123-45-67890")
                        .businessName("김사장 식당")
                        .ownerName("김사장")
                        .phoneNumber("010-1234-5678")
                        .address("서울시 강남구")
                        .enabled(true)
                        .build();
                userRepository.save(kimceo);
                log.info("Created store owner user: username=kimceo, password=123456");
                
                // 유통업자 계정 생성 (youtong1)
                User youtong1 = User.builder()
                        .username("youtong1")
                        .password(passwordEncoder.encode("123456"))
                        .email("youtong1@example.com")
                        .userType(com.example.payflow.security.domain.UserType.DISTRIBUTOR)
                        .roles(Set.of(Role.ROLE_DISTRIBUTOR))
                        .businessNumber("201-11-11111")
                        .businessName("서울농산물유통")
                        .ownerName("이유통")
                        .phoneNumber("010-2001-1111")
                        .address("서울시 송파구 가락동")
                        .enabled(true)
                        .build();
                userRepository.save(youtong1);
                log.info("Created distributor user: username=youtong1, password=123456");
                
                // 유통업자 계정 생성 (youtong2)
                User youtong2 = User.builder()
                        .username("youtong2")
                        .password(passwordEncoder.encode("123456"))
                        .email("youtong2@example.com")
                        .userType(com.example.payflow.security.domain.UserType.DISTRIBUTOR)
                        .roles(Set.of(Role.ROLE_DISTRIBUTOR))
                        .businessNumber("202-22-22222")
                        .businessName("부산수산물유통")
                        .ownerName("박수산")
                        .phoneNumber("010-2002-2222")
                        .address("부산시 수영구")
                        .enabled(true)
                        .build();
                userRepository.save(youtong2);
                log.info("Created distributor user: username=youtong2, password=123456");
                
                // 유통업자 계정 생성 (youtong3)
                User youtong3 = User.builder()
                        .username("youtong3")
                        .password(passwordEncoder.encode("123456"))
                        .email("youtong3@example.com")
                        .userType(com.example.payflow.security.domain.UserType.DISTRIBUTOR)
                        .roles(Set.of(Role.ROLE_DISTRIBUTOR))
                        .businessNumber("203-33-33333")
                        .businessName("대구축산물유통")
                        .ownerName("최축산")
                        .phoneNumber("010-2003-3333")
                        .address("대구시 북구")
                        .enabled(true)
                        .build();
                userRepository.save(youtong3);
                log.info("Created distributor user: username=youtong3, password=123456");
                
                // 유통업자 계정 생성 (youtong4)
                User youtong4 = User.builder()
                        .username("youtong4")
                        .password(passwordEncoder.encode("123456"))
                        .email("youtong4@example.com")
                        .userType(com.example.payflow.security.domain.UserType.DISTRIBUTOR)
                        .roles(Set.of(Role.ROLE_DISTRIBUTOR))
                        .businessNumber("204-44-44444")
                        .businessName("인천냉동식품유통")
                        .ownerName("정냉동")
                        .phoneNumber("010-2004-4444")
                        .address("인천시 남동구")
                        .enabled(true)
                        .build();
                userRepository.save(youtong4);
                log.info("Created distributor user: username=youtong4, password=123456");
                
                // 유통업자 계정 생성 (youtong5)
                User youtong5 = User.builder()
                        .username("youtong5")
                        .password(passwordEncoder.encode("123456"))
                        .email("youtong5@example.com")
                        .userType(com.example.payflow.security.domain.UserType.DISTRIBUTOR)
                        .roles(Set.of(Role.ROLE_DISTRIBUTOR))
                        .businessNumber("205-55-55555")
                        .businessName("광주청과물유통")
                        .ownerName("강청과")
                        .phoneNumber("010-2005-5555")
                        .address("광주시 서구")
                        .enabled(true)
                        .build();
                userRepository.save(youtong5);
                log.info("Created distributor user: username=youtong5, password=123456");
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
