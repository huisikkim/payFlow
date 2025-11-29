package com.example.payflow.security.config;

import com.example.payflow.distributor.application.DistributorService;
import com.example.payflow.inventory.application.InventoryService;
import com.example.payflow.security.domain.Role;
import com.example.payflow.security.domain.User;
import com.example.payflow.security.domain.UserRepository;
import com.example.payflow.store.application.StoreService;
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
    private final StoreService storeService;
    private final DistributorService distributorService;
    
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
            
            // kimceo 계정의 매장 등록
            try {
                log.info("🏪 kimceo 계정의 매장 등록 중...");
                storeService.registerOrUpdateStoreInfo(
                    "kimceo",  // storeId를 username과 동일하게 설정
                    "김사장 식당",
                    "김사장",
                    "한식",
                    "서울시 강남구",
                    "쌀,채소,육류,조미료",
                    "강남역 근처 한식당입니다. 매일 신선한 재료로 요리합니다.",
                    5,
                    "11:00-22:00",
                    "010-1234-5678",
                    "서울시 강남구 강남대로 123"
                );
                log.info("✅ kimceo 계정의 매장 등록 완료");
            } catch (Exception e) {
                log.warn("매장 등록 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
            }
            
            // youtong1 유통업체 등록
            try {
                log.info("🚚 youtong1 유통업체 등록 중...");
                distributorService.registerOrUpdateDistributorInfo(
                    "youtong1",
                    "서울농산물유통",
                    "쌀/곡물,채소,과일",
                    "서울,경기,인천",
                    true,
                    "배송비 무료 (30만원 이상), 익일 배송",
                    "가락시장 직송! 신선한 농산물을 공급합니다. 30년 전통의 믿을 수 있는 유통업체입니다.",
                    "HACCP,GAP인증",
                    300000,
                    "06:00-18:00",
                    "010-2001-1111",
                    "youtong1@example.com",
                    "서울시 송파구 가락동 가락시장 A동 123호"
                );
                log.info("✅ youtong1 유통업체 등록 완료");
            } catch (Exception e) {
                log.warn("유통업체 등록 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
            }
            
            // youtong2 유통업체 등록
            try {
                log.info("🚚 youtong2 유통업체 등록 중...");
                distributorService.registerOrUpdateDistributorInfo(
                    "youtong2",
                    "부산수산물유통",
                    "수산물,해산물,냉동식품",
                    "부산,경남,울산",
                    true,
                    "배송비 3만원 (50만원 이상 무료), 당일/익일 배송",
                    "부산 자갈치시장 직송! 신선한 수산물을 전국으로 공급합니다.",
                    "HACCP,ISO22000",
                    500000,
                    "05:00-17:00",
                    "010-2002-2222",
                    "youtong2@example.com",
                    "부산시 수영구 수산시장로 456"
                );
                log.info("✅ youtong2 유통업체 등록 완료");
            } catch (Exception e) {
                log.warn("유통업체 등록 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
            }
            
            // youtong3 유통업체 등록
            try {
                log.info("🚚 youtong3 유통업체 등록 중...");
                distributorService.registerOrUpdateDistributorInfo(
                    "youtong3",
                    "대구축산물유통",
                    "육류,돈육,계육,한우",
                    "대구,경북,경남",
                    true,
                    "배송비 5만원 (100만원 이상 무료), 냉장차량 배송",
                    "1등급 한우 전문! 신선한 축산물을 냉장 배송합니다. HACCP 인증 시설 보유.",
                    "HACCP,축산물이력제",
                    1000000,
                    "07:00-19:00",
                    "010-2003-3333",
                    "youtong3@example.com",
                    "대구시 북구 축산로 789"
                );
                log.info("✅ youtong3 유통업체 등록 완료");
            } catch (Exception e) {
                log.warn("유통업체 등록 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
            }
            
            // youtong4 유통업체 등록
            try {
                log.info("🚚 youtong4 유통업체 등록 중...");
                distributorService.registerOrUpdateDistributorInfo(
                    "youtong4",
                    "인천냉동식품유통",
                    "냉동식품,가공식품,조미료,소스",
                    "인천,서울,경기",
                    true,
                    "배송비 2만원 (20만원 이상 무료), 냉동차량 배송",
                    "다양한 냉동식품과 가공식품을 취급합니다. 대량 주문 환영!",
                    "HACCP,ISO9001",
                    200000,
                    "08:00-20:00",
                    "010-2004-4444",
                    "youtong4@example.com",
                    "인천시 남동구 냉동물류단지 101호"
                );
                log.info("✅ youtong4 유통업체 등록 완료");
            } catch (Exception e) {
                log.warn("유통업체 등록 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
            }
            
            // youtong5 유통업체 등록
            try {
                log.info("🚚 youtong5 유통업체 등록 중...");
                distributorService.registerOrUpdateDistributorInfo(
                    "youtong5",
                    "광주청과물유통",
                    "채소,과일,청과물",
                    "광주,전남,전북",
                    true,
                    "배송비 무료 (40만원 이상), 새벽 배송 가능",
                    "나주 배, 담양 딸기 등 지역 특산물 전문! 신선한 청과물을 새벽에 배송합니다.",
                    "GAP인증,친환경인증",
                    400000,
                    "04:00-16:00",
                    "010-2005-5555",
                    "youtong5@example.com",
                    "광주시 서구 청과시장로 202호"
                );
                log.info("✅ youtong5 유통업체 등록 완료");
            } catch (Exception e) {
                log.warn("유통업체 등록 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
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
