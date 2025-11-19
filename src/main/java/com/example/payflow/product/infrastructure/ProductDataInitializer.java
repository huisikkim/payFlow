package com.example.payflow.product.infrastructure;

import com.example.payflow.product.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductDataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    
    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("상품 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("Pick Swap 샘플 상품 데이터 초기화 시작...");
        
        // 전자기기
        createProduct("아이폰 14 Pro 256GB", "거의 새것 같은 상태입니다. 케이스와 함께 드립니다.",
                new BigDecimal("950000"), ProductCategory.ELECTRONICS, ProductCondition.LIKE_NEW,
                1L, "김철수", "서울 강남구",
                Arrays.asList("https://picsum.photos/400/400?random=1", "https://picsum.photos/400/400?random=2"));
        
        createProduct("갤럭시 버즈 프로", "한 달 사용했습니다. 깨끗합니다.",
                new BigDecimal("120000"), ProductCategory.ELECTRONICS, ProductCondition.LIKE_NEW,
                2L, "이영희", "서울 송파구",
                Arrays.asList("https://picsum.photos/400/400?random=3"));
        
        createProduct("맥북 에어 M1", "2021년 모델, 사용감 거의 없음",
                new BigDecimal("850000"), ProductCategory.ELECTRONICS, ProductCondition.GOOD,
                3L, "박민수", "경기 성남시",
                Arrays.asList("https://picsum.photos/400/400?random=4", "https://picsum.photos/400/400?random=5"));
        
        // 패션
        createProduct("나이키 에어포스 1 (270mm)", "새 상품, 한 번도 신지 않았습니다.",
                new BigDecimal("89000"), ProductCategory.FASHION, ProductCondition.NEW,
                1L, "김철수", "서울 강남구",
                Arrays.asList("https://picsum.photos/400/400?random=6"));
        
        createProduct("노스페이스 패딩 (95)", "작년 겨울 시즌 제품, 상태 좋음",
                new BigDecimal("150000"), ProductCategory.FASHION, ProductCondition.GOOD,
                4L, "최지은", "서울 마포구",
                Arrays.asList("https://picsum.photos/400/400?random=7", "https://picsum.photos/400/400?random=8"));
        
        // 가전제품
        createProduct("다이슨 청소기 V11", "2년 사용, 정상 작동합니다.",
                new BigDecimal("280000"), ProductCategory.HOME_APPLIANCES, ProductCondition.GOOD,
                5L, "정수진", "서울 서초구",
                Arrays.asList("https://picsum.photos/400/400?random=9"));
        
        createProduct("LG 스타일러", "이사로 인해 판매합니다. 거의 새것",
                new BigDecimal("650000"), ProductCategory.HOME_APPLIANCES, ProductCondition.LIKE_NEW,
                2L, "이영희", "서울 송파구",
                Arrays.asList("https://picsum.photos/400/400?random=10", "https://picsum.photos/400/400?random=11"));
        
        // 가구
        createProduct("이케아 책상 (120x60)", "조립식 책상, 상태 양호",
                new BigDecimal("45000"), ProductCategory.FURNITURE, ProductCondition.FAIR,
                6L, "강동욱", "경기 고양시",
                Arrays.asList("https://picsum.photos/400/400?random=12"));
        
        createProduct("한샘 3인용 소파", "깨끗하게 사용했습니다. 직거래만 가능",
                new BigDecimal("180000"), ProductCategory.FURNITURE, ProductCondition.GOOD,
                3L, "박민수", "경기 성남시",
                Arrays.asList("https://picsum.photos/400/400?random=13", "https://picsum.photos/400/400?random=14"));
        
        // 도서
        createProduct("클린 코드 (Clean Code)", "프로그래밍 필독서, 밑줄 없음",
                new BigDecimal("25000"), ProductCategory.BOOKS, ProductCondition.GOOD,
                7L, "윤서준", "서울 관악구",
                Arrays.asList("https://picsum.photos/400/400?random=15"));
        
        createProduct("해리포터 전집 (7권)", "전권 세트, 상태 좋음",
                new BigDecimal("60000"), ProductCategory.BOOKS, ProductCondition.GOOD,
                4L, "최지은", "서울 마포구",
                Arrays.asList("https://picsum.photos/400/400?random=16"));
        
        // 스포츠
        createProduct("요가매트 + 요가블록 세트", "거의 사용 안 함",
                new BigDecimal("35000"), ProductCategory.SPORTS, ProductCondition.LIKE_NEW,
                8L, "한지민", "서울 용산구",
                Arrays.asList("https://picsum.photos/400/400?random=17"));
        
        createProduct("캠핑 텐트 4인용", "2회 사용, 깨끗합니다",
                new BigDecimal("120000"), ProductCategory.SPORTS, ProductCondition.LIKE_NEW,
                5L, "정수진", "서울 서초구",
                Arrays.asList("https://picsum.photos/400/400?random=18", "https://picsum.photos/400/400?random=19"));
        
        // 뷰티
        createProduct("다이슨 에어랩", "선물받았는데 사용 안 해서 판매합니다",
                new BigDecimal("380000"), ProductCategory.BEAUTY, ProductCondition.NEW,
                9L, "송하나", "서울 강동구",
                Arrays.asList("https://picsum.photos/400/400?random=20"));
        
        // 반려동물
        createProduct("강아지 이동장 (중형견용)", "깨끗하게 사용했습니다",
                new BigDecimal("45000"), ProductCategory.PET_SUPPLIES, ProductCondition.GOOD,
                6L, "강동욱", "경기 고양시",
                Arrays.asList("https://picsum.photos/400/400?random=21"));
        
        log.info("Pick Swap 샘플 상품 데이터 초기화 완료!");
    }
    
    private void createProduct(String title, String description, BigDecimal price,
                               ProductCategory category, ProductCondition condition,
                               Long sellerId, String sellerName, String location,
                               List<String> imageUrls) {
        Product product = new Product(title, description, price, category, condition,
                sellerId, sellerName, location, imageUrls);
        
        // 랜덤하게 조회수, 좋아요 수 설정 (테스트용)
        for (int i = 0; i < (int)(Math.random() * 50); i++) {
            product.incrementViewCount();
        }
        for (int i = 0; i < (int)(Math.random() * 20); i++) {
            product.incrementLikeCount();
        }
        
        productRepository.save(product);
    }
}
