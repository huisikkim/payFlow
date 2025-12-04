package com.example.payflow.security.config;

import com.example.payflow.catalog.application.ProductCatalogService;
import com.example.payflow.catalog.domain.ProductCatalog;
import com.example.payflow.catalog.presentation.dto.CreateProductRequest;
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
    private final ProductCatalogService productCatalogService;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                // 일반 사용자 생성
                User user = User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("password"))
                        .email("user@example.com")
                        .nickname("호랑이")
                        .roles(Set.of(Role.ROLE_USER))
                        .enabled(true)
                        .build();
                userRepository.save(user);
                log.info("Created default user: username=user, password=password, nickname=호랑이");
                
                // 관리자 생성
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("admin@example.com")
                        .nickname("사자")
                        .roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.info("Created default admin: username=admin, password=admin, nickname=사자");
                
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
            
            // youtong1 상품 등록 (농산물)
            try {
                log.info("📦 youtong1 상품 등록 중...");
                createProduct("youtong1", "신동진 쌀 10kg", "쌀/곡물", "2024년산 햅쌀, 밥맛이 좋은 프리미엄 쌀", 45000L, "박스", 500, "국내산(전북)", "신동진", "HACCP");
                createProduct("youtong1", "찰현미 5kg", "쌀/곡물", "영양가 높은 찰현미, 건강식", 38000L, "박스", 300, "국내산(충남)", "찰현미", "GAP인증");
                createProduct("youtong1", "양파 20kg", "채소", "햇양파, 단맛이 강한 무안 양파", 25000L, "박스", 800, "국내산(무안)", "무안양파", "GAP인증");
                createProduct("youtong1", "감자 20kg", "채소", "수미 감자, 요리용으로 최적", 30000L, "박스", 600, "국내산(강원)", "강원감자", "GAP인증");
                createProduct("youtong1", "당근 10kg", "채소", "신선한 당근, 단맛이 좋음", 28000L, "박스", 400, "국내산(제주)", "제주당근", "GAP인증");
                createProduct("youtong1", "사과 10kg", "과일", "부사 사과 36-40입, 아삭하고 달콤함", 55000L, "박스", 350, "국내산(충주)", "충주사과", "GAP인증");
                createProduct("youtong1", "배 10kg", "과일", "신고배 12-15입, 과즙이 풍부함", 60000L, "박스", 250, "국내산(나주)", "나주배", "GAP인증");
                createProduct("youtong1", "대파 5kg", "채소", "신선한 대파, 향이 좋음", 15000L, "박스", 700, "국내산(대관령)", "대관령대파", "GAP인증");
                createProduct("youtong1", "깐마늘 5kg", "채소", "깐마늘, 요리용으로 편리", 35000L, "박스", 450, "국내산(의성)", "의성마늘", "GAP인증");
                createProduct("youtong1", "꿀고구마 10kg", "채소", "꿀고구마, 당도가 높음", 32000L, "박스", 550, "국내산(해남)", "해남고구마", "GAP인증");
                log.info("✅ youtong1 상품 10개 등록 완료");
            } catch (Exception e) {
                log.warn("상품 등록 중 오류: {}", e.getMessage());
            }
            
            // youtong2 상품 등록 (수산물)
            try {
                log.info("📦 youtong2 상품 등록 중...");
                createProduct("youtong2", "고등어 1kg", "수산물", "신선한 국산 고등어 4-5마리, 구이용", 18000L, "kg", 400, "국내산(부산)", "부산고등어", "HACCP");
                createProduct("youtong2", "갈치 1kg", "수산물", "은갈치 2-3마리, 살이 통통함", 45000L, "kg", 200, "국내산(제주)", "제주갈치", "HACCP");
                createProduct("youtong2", "오징어 1kg", "수산물", "냉동 오징어 5-7마리, 손질된 상태", 28000L, "kg", 600, "국내산(동해)", "동해오징어", "HACCP");
                createProduct("youtong2", "새우 1kg", "수산물", "흰다리새우, 대하급", 35000L, "kg", 350, "국내산(서해)", "서해새우", "HACCP");
                createProduct("youtong2", "조기 1kg", "수산물", "참조기 5-6마리, 구이/찌개용", 42000L, "kg", 280, "국내산(영광)", "영광조기", "HACCP");
                createProduct("youtong2", "명태 1kg", "수산물", "냉동 명태 3-4마리, 찌개용", 22000L, "kg", 500, "러시아산", "러시아명태", "HACCP");
                createProduct("youtong2", "꽃게 1kg", "수산물", "살아있는 꽃게 4-5마리, 찜용", 55000L, "kg", 180, "국내산(서해)", "서해꽃게", "HACCP");
                createProduct("youtong2", "광어 1kg", "수산물", "활광어, 회/구이용", 38000L, "kg", 220, "국내산(제주)", "제주광어", "HACCP");
                createProduct("youtong2", "문어 1kg", "수산물", "냉동 문어, 데쳐서 사용", 48000L, "kg", 300, "국내산(남해)", "남해문어", "HACCP");
                createProduct("youtong2", "멸치 1kg", "수산물", "국물용 멸치, 중멸", 32000L, "kg", 450, "국내산(남해)", "남해멸치", "HACCP");
                log.info("✅ youtong2 상품 10개 등록 완료");
            } catch (Exception e) {
                log.warn("상품 등록 중 오류: {}", e.getMessage());
            }
            
            // youtong3 상품 등록 (축산물)
            try {
                log.info("📦 youtong3 상품 등록 중...");
                createProduct("youtong3", "한우 등심 1kg", "육류", "1등급 한우 등심, 구이용", 85000L, "kg", 150, "국내산(대구)", "한우1등급", "HACCP,축산물이력제");
                createProduct("youtong3", "한우 안심 1kg", "육류", "1등급 한우 안심, 스테이크용", 95000L, "kg", 120, "국내산(대구)", "한우1등급", "HACCP,축산물이력제");
                createProduct("youtong3", "한우 불고기 1kg", "육류", "1등급 한우 불고기용", 65000L, "kg", 200, "국내산(대구)", "한우1등급", "HACCP,축산물이력제");
                createProduct("youtong3", "돼지 삼겹살 1kg", "육류", "국내산 돼지 삼겹살", 18000L, "kg", 400, "국내산(경북)", "국내산돼지", "HACCP");
                createProduct("youtong3", "돼지 목살 1kg", "육류", "국내산 돼지 목살, 구이용", 16000L, "kg", 380, "국내산(경북)", "국내산돼지", "HACCP");
                createProduct("youtong3", "닭가슴살 1kg", "육류", "신선한 닭가슴살, 다이어트용", 12000L, "kg", 500, "국내산(경북)", "국내산닭", "HACCP");
                createProduct("youtong3", "닭다리 1kg", "육류", "신선한 닭다리, 튀김/구이용", 9000L, "kg", 550, "국내산(경북)", "국내산닭", "HACCP");
                createProduct("youtong3", "소갈비 1kg", "육류", "1등급 한우 갈비, 찜/구이용", 78000L, "kg", 100, "국내산(대구)", "한우1등급", "HACCP,축산물이력제");
                createProduct("youtong3", "양념 돼지갈비 1kg", "육류", "양념된 돼지갈비, 바로 구워먹기", 22000L, "kg", 300, "국내산(경북)", "국내산돼지", "HACCP");
                createProduct("youtong3", "소고기 국거리 1kg", "육류", "한우 국거리, 국/찌개용", 45000L, "kg", 250, "국내산(대구)", "한우1등급", "HACCP,축산물이력제");
                log.info("✅ youtong3 상품 10개 등록 완료");
            } catch (Exception e) {
                log.warn("상품 등록 중 오류: {}", e.getMessage());
            }
            
            // youtong4 상품 등록 (냉동/가공식품)
            try {
                log.info("📦 youtong4 상품 등록 중...");
                createProduct("youtong4", "냉동 만두 2kg", "냉동식품", "왕교자 만두, 대용량", 15000L, "봉지", 800, "국내산", "왕교자", "HACCP");
                createProduct("youtong4", "냉동 피자 400g", "냉동식품", "콤비네이션 피자, 업소용", 12000L, "판", 600, "국내산", "업소용피자", "HACCP");
                createProduct("youtong4", "냉동 감자튀김 2.5kg", "냉동식품", "크링클컷 감자튀김", 18000L, "봉지", 700, "미국산", "크링클컷", "HACCP");
                createProduct("youtong4", "냉동 새우튀김 1kg", "냉동식품", "왕새우튀김 20-25개, 튀김옷 입힌 상태", 28000L, "봉지", 450, "베트남산", "왕새우튀김", "HACCP");
                createProduct("youtong4", "진간장 5L", "조미료", "진간장, 업소용 대용량", 25000L, "통", 500, "국내산", "진간장", "HACCP");
                createProduct("youtong4", "고추장 5kg", "조미료", "태양초 고추장, 매운맛", 32000L, "통", 400, "국내산", "태양초", "HACCP");
                createProduct("youtong4", "된장 5kg", "조미료", "재래식 된장, 찌개용", 28000L, "통", 380, "국내산", "재래된장", "HACCP");
                createProduct("youtong4", "식용유 18L", "조미료", "대두유, 튀김용", 22000L, "통", 550, "국내산", "대두유", "HACCP");
                createProduct("youtong4", "참기름 1L", "조미료", "100% 참깨 참기름", 45000L, "병", 300, "국내산", "참기름", "HACCP");
                createProduct("youtong4", "냉동 치킨너겟 1kg", "냉동식품", "치킨너겟, 어린이 간식용", 16000L, "봉지", 650, "국내산", "치킨너겟", "HACCP");
                log.info("✅ youtong4 상품 10개 등록 완료");
            } catch (Exception e) {
                log.warn("상품 등록 중 오류: {}", e.getMessage());
            }
            
            // youtong5 상품 등록 (청과물)
            try {
                log.info("📦 youtong5 상품 등록 중...");
                createProduct("youtong5", "청상추 2kg", "채소", "신선한 청상추, 쌈용", 12000L, "박스", 600, "국내산(광주)", "청상추", "GAP인증");
                createProduct("youtong5", "깻잎 1kg", "채소", "향긋한 깻잎, 쌈용", 15000L, "박스", 500, "국내산(전남)", "깻잎", "GAP인증");
                createProduct("youtong5", "시금치 2kg", "채소", "신선한 시금치, 나물용", 18000L, "박스", 450, "국내산(광주)", "시금치", "GAP인증");
                createProduct("youtong5", "딸기 2kg", "과일", "담양 딸기, 당도 높음", 35000L, "박스", 300, "국내산(담양)", "담양딸기", "GAP인증");
                createProduct("youtong5", "토마토 5kg", "과일", "완숙 토마토, 샐러드용", 28000L, "박스", 400, "국내산(전남)", "완숙토마토", "GAP인증");
                createProduct("youtong5", "오이 10kg", "채소", "신선한 오이, 샐러드/무침용", 22000L, "박스", 550, "국내산(광주)", "오이", "GAP인증");
                createProduct("youtong5", "애호박 10kg", "채소", "애호박, 찌개/볶음용", 20000L, "박스", 500, "국내산(전남)", "애호박", "GAP인증");
                createProduct("youtong5", "파프리카 5kg", "채소", "컬러 파프리카, 샐러드용", 32000L, "박스", 350, "국내산(광주)", "파프리카", "GAP인증");
                createProduct("youtong5", "브로콜리 5kg", "채소", "신선한 브로콜리, 샐러드용", 25000L, "박스", 400, "국내산(전남)", "브로콜리", "GAP인증");
                createProduct("youtong5", "양배추 10kg", "채소", "신선한 양배추, 샐러드/쌈용", 18000L, "박스", 600, "국내산(광주)", "양배추", "GAP인증");
                log.info("✅ youtong5 상품 10개 등록 완료");
            } catch (Exception e) {
                log.warn("상품 등록 중 오류: {}", e.getMessage());
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
    
    private void createProduct(String distributorId, String productName, String category, 
                              String description, Long unitPrice, String unit, Integer stockQuantity,
                              String origin, String brand, String certifications) {
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName(productName);
        request.setCategory(category);
        request.setDescription(description);
        request.setUnitPrice(unitPrice);
        request.setUnit(unit);
        request.setStockQuantity(stockQuantity);
        request.setOrigin(origin);
        request.setBrand(brand);
        request.setIsAvailable(true);
        request.setMinOrderQuantity(1);
        request.setMaxOrderQuantity(100);
        request.setCertifications(certifications);
        
        productCatalogService.createProduct(distributorId, request);
    }
}
